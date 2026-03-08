package io.github.pubsubseekbucket.subscribe.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.ProjectSnapshotName;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.ResourceUtils;
import io.github.pubsubseekbucket.EventBusConfigurationNnx;
import io.github.pubsubseekbucket.publish.PublisherFactory;
import io.github.pubsubseekbucket.publish.persisted.OutboxDrainer;
import io.github.pubsubseekbucket.publish.persisted.PersistedPublisher;
import io.github.pubsubseekbucket.publish.persisted.PersistedPublisherConfiguration;
import io.github.pubsubseekbucket.publish.statedump.StateDumpService;
import io.github.pubsubseekbucket.subscribe.EventHandler;
import io.github.pubsubseekbucket.subscribe.SubscriberFactory;
import io.github.pubsubseekbucket.subscribe.statedump.HistoricalValuationPriceEventV1;
import io.github.pubsubseekbucket.subscribe.statedump.StateDumpReader;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Application.class, TestConfig.class, EventBusConfigurationNnx.class, PersistedPublisherConfiguration.class, CredentialsProviderTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Help trigger life-cycle issues
public class IntegrationTest {
    private static final String INSTRUMENT_DUMP_NAME = "dump_20211211T020009.279Z";
    private static final String VALUATION_PRICE_DUMP_NAME = "dump_20240216T063021.840Z";
    private static final String BUCKET_NAME = "bucketName";
    private static final String VALUATION_PRICE_BUCKET_NAME = "valuationPriceBucketName";

    @Autowired
    private Storage storage;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriptionAdminUtil subscriptionAdminUtil;

    @Autowired
    private SubscriberFactory subscriberFactory;

    @Autowired
    private PublisherFactory publisherFactory;

    @Autowired
    private OutboxDrainer outboxDrainer;

    @Autowired
    private PersistedPublisher<String> testPublisher;

    @Value("${embedded.google.pubsub.topics-and-subscriptions[0].topic}")
    private String topicId;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void subscribe() {
        // Given
        var subscription = subscriptionAdminUtil.createDynamicSubscription(topicId, "dynamicSubscriptions", "team");

        EventHandler<String> eventHandler = mock(EventHandler.class);
        when(eventHandler.handle(any())).thenReturn(CompletableFuture.completedFuture(null));

        var subscriber = subscriberFactory.createSubscriber(subscription, eventHandler, new TypeReference<>() {
        });

        // When
        subscriber.subscribe();
        publisherFactory.createPublisher(topicId).publish("test");

        // Then
        verify(eventHandler, timeout(5000)).handle("test");
    }

    @Test
    public void outboxDrainerHasAllPublishers() {
        // Given
        var publisherThatIsNotBean = publisherFactory.createPersistedPublisher(topicId, Object::toString, event -> 1L);

        assertEquals(testPublisher, outboxDrainer.getPublisher("test-topic").orElseThrow());
        assertEquals(publisherThatIsNotBean, outboxDrainer.getPublisher(topicId).orElseThrow());
    }

    @Test
    public void readStateDumpWithMultipleFiles() {
        // Given
        createDump(storage, BUCKET_NAME, INSTRUMENT_DUMP_NAME, "instrument-state-dumps",
                "instruments_1.jsonl", "instruments_2.jsonl");

        // When
        var stateDumpReader = new StateDumpReader<>(BUCKET_NAME, InstrumentEvent.class, storage, objectMapper);
        var dump = stateDumpReader.findLatestStateDump();
        assertTrue(dump.isPresent());

        // Then
        assertEquals(4, stateDumpReader.stream(dump.get()).toList().size());
        assertEquals(INSTRUMENT_DUMP_NAME, dump.get());
    }

    @Test
    public void readStateDumpWithEmptyPrefix() {
        // Given
        String dumpName = "20250924_T_090834.529Z";
        createDump(storage, BUCKET_NAME, dumpName, "locked-operation-state-dump",
                "lockedOperationsEvents.jsonl");

        // When
        var stateDumpReader = new StateDumpReader<>(BUCKET_NAME, LockEvent.class, storage, objectMapper, "");
        var dump = stateDumpReader.findLatestStateDump();
        assertTrue(dump.isPresent());

        // Then
        assertEquals(36, stateDumpReader.stream(dump.get()).toList().size());
        assertEquals(dumpName, dump.get());
    }

    @Test
    public void readCompressedStateDump() {
        // Given
        objectMapper.registerModule(new JavaTimeModule());
        createDump(storage, VALUATION_PRICE_BUCKET_NAME, VALUATION_PRICE_DUMP_NAME, "historical-valuation-price-state-dump",
                "prices_2024_02.jsonl.gz");

        // When
        var stateDumpReader = new StateDumpReader<>(VALUATION_PRICE_BUCKET_NAME, HistoricalValuationPriceEventV1.class, storage, objectMapper);
        var dump = stateDumpReader.findLatestStateDump();
        assertTrue(dump.isPresent());

        // Then
        assertEquals(3, stateDumpReader.stream(dump.get()).toList().size());
        assertEquals(VALUATION_PRICE_DUMP_NAME, dump.get());
    }

    @Test
    public void readStateDumpWithFilter() {
        // Given
        createDumps(storage, "another-bucket", "dummy.jsonl", "001", "002", "003");

        // When
        var stateDumpReader = new StateDumpReader<>("another-bucket", storage, str -> str);
        var dump = stateDumpReader.findLatestStateDump(blobInfo -> !StringUtils.contains(blobInfo.getName(), "003"));
        assertTrue(dump.isPresent());

        // Then
        var dumpContent = stateDumpReader.stream(dump.get()).toList();
        assertEquals(1, dumpContent.size());
        assertEquals("dump_002/dummy.jsonl", dumpContent.getFirst());
        assertEquals("dump_002", dump.get());
    }

    @Test
    public void writeStateDump() {
        // Given
        var instruments = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).map(instrumentId -> InstrumentEvent.builder().instrumentId(instrumentId).build()).toList();
        var stateDumpWriter = new StateDumpService<>("instruments.jsonl", (BiConsumer<Integer, Consumer<InstrumentEvent>>) (integer, consumer) -> instruments.forEach(consumer), storage, objectMapper,
                subscriptionAdminUtil, mock(MeterRegistry.class));

        // When
        var result = stateDumpWriter.performStateDump(topicId, BUCKET_NAME, 1);

        // Then
        assertTrue(result.isSuccess());

        String actualDumpFileContents = new String(storage.get(result.getStateDumpBlob().getBlobId()).getContent(), StandardCharsets.UTF_8);
        String expectedDumpFileContents = instruments.stream().map(this::serialize).map(line -> line + System.lineSeparator()).collect(Collectors.joining());
        assertEquals(expectedDumpFileContents, actualDumpFileContents);

        assertTrue(subscriptionAdminUtil.listSnapshots(topicId).stream()
                .map(s -> ProjectSnapshotName.parse(s.getName()).getSnapshot())
                .anyMatch(snapshotName -> result.getSnapshotName().equals(snapshotName)));
    }

    @Test
    public void dynamicSubscriptions() {
        // Given
        var subscription = subscriptionAdminUtil.createDynamicSubscription(topicId, "dynamicSubscriptions", "team");

        subscriptionAdminUtil.createSnapshot(subscription, "foo", Map.of());

        // When
        var subscriber = subscriberFactory.createSubscriber(
                subscription,
                (EventHandler<String>) event -> null,
                new TypeReference<>() {
                });

        // Then
        assertTrue(subscriber.seekToSnapshot("foo"));
    }

    @Test
    public void deleteSubscription() {
        var subscription = subscriptionAdminUtil.createSubscription(topicId, "to_be_deleted", "team");

        subscriptionAdminUtil.createSnapshot(subscription, "foo", Map.of());

        // When
        var subscriber = subscriberFactory.createSubscriber(
                subscription,
                (EventHandler<String>) event -> null,
                new TypeReference<>() {
                });

        // Then
        assertTrue(subscriber.seekToSnapshot("foo"));

        subscriber.subscribe();
        subscriptionAdminUtil.deleteSubscription("to_be_deleted");

        verify(applicationEventPublisher, timeout(500).times(2)).publishEvent(any());
    }

    @SneakyThrows
    private String serialize(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    private void createDump(Storage storage, String bucketName, String dumpName, String classPath, String... fileNames) {
        Arrays.stream(fileNames).forEach(fileName -> {
            String blobName = dumpName + "/" + fileName;

            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            storage.create(blobInfo, getBytes(classPath, blobName));
        });
    }

    private void createDumps(Storage storage, String bucketName, String fileName, String... dumpNames) {
        Arrays.stream(dumpNames).forEach(dumpName -> {
            String blobName = "dump_" + dumpName + "/" + fileName;

            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            storage.create(blobInfo, blobName.getBytes(StandardCharsets.UTF_8));
        });
    }

    @SneakyThrows
    private static byte[] getBytes(String classPath, String fileName) {
        return Files.readAllBytes(ResourceUtils.getFile("classpath:" + classPath + "/" + fileName).toPath());
    }
}
