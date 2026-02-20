package io.github.pubsubseekbucket.publish.persisted;

import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import io.github.pubsubseekbucket.publish.persisted.TestData.SampleEnvelope;
import io.github.pubsubseekbucket.subscribe.integrationtest.Application;
import io.github.pubsubseekbucket.util.Deserializer;
import se.bmd.trading.testsupport.EventConsumer;
import se.bmd.trading.testsupport.PubsubTestManager;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.pubsubseekbucket.publish.persisted.TestData.createEnvelope;
import static io.github.pubsubseekbucket.publish.persisted.TestData.getUniqueIds;
import static io.github.pubsubseekbucket.publish.persisted.TestData.nextUniqueId;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {Application.class, PersistedPublisherConfiguration.class, PersistedPublisherTestConfig.class, PubsubTestManager.class},
        properties = {
                "MY_POD_NAME=TEST_POD",
                "eventbus.publish.republishThresholdInSeconds=1",
                "eventbus.publish.drainFrequencyInSeconds=1"})
class PersistedPublisherTest {

    private static final String INSERT_IN_OUTBOX = """
            insert into outbox(data, created, id, version, topic_name)
                values(:data, current_timestamp, :id, :version, :topicName)""";

    @Autowired
    OutboxDao outboxDao;
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    protected PubsubTestManager pubsubTestManager;

    @Value("${embedded.google.pubsub.topics-and-subscriptions[0].topic}")
    private String topicId;

    @Value("${embedded.google.pubsub.topics-and-subscriptions[0].subscription}")
    private String subscriptionId;

    String testName;

    SampleEnvelopeConsumer sampleEnvelopeConsumer;

    @Autowired
    PersistedPublisher<SampleEnvelope> target;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    Deserializer<String, SampleEnvelope> deserializer;
    @Autowired
    private PublishTriggerRepository publishTriggerRepository;

    @BeforeEach
    protected void beforeEach(TestInfo testInfo) {
        jdbcTemplate.update("DELETE FROM outbox", Map.of());

        testName = testInfo.getTestClass().get().getSimpleName() + "_" + testInfo.getTestMethod().get().getName();

        sampleEnvelopeConsumer = new SampleEnvelopeConsumer();

        pubsubTestManager.addSubscriber(topicId, sampleEnvelopeConsumer, testName);

        publishTriggerRepository.clear();

        log.info("================================================================");
        log.info("Starting test {}", testName);
        log.info("----------------------------------------------------------------");
    }

    @AfterEach
    void cleanup() {
        pubsubTestManager.cleanupAfterTest();
    }

    @Test
    void publishShouldPublishAndRemoveFromOutboxIfPublished() {

        // Given
        var id = nextUniqueId();
        var msg = createEnvelope(id);

        // When
        var publishTrigger = runInTransaction(status -> target.persistEvent(msg));
        assertEquals(1, outboxDao.getOutboxSize());

        publishTrigger.publish();

        // Then
        sampleEnvelopeConsumer.getEvents(1, e -> e.sequenceNumber() == id, Duration.ofSeconds(5L))
                .stream()
                .sorted(Comparator.comparing(SampleEnvelope::sequenceNumber))
                .forEach(e -> assertEquals(msg, e));

        assertEquals(0, outboxDao.getOutboxSize());
    }

    @Test
    void publishManyShouldPublishAndRemoveFromOutboxIfPublished() {

        // Given
        var ids = getUniqueIds(100);
        var messages = ids.stream().map(TestData::createEnvelope).toList();

        // When
        var publishTrigger = runInTransaction(status -> target.persistEvents(messages));

        assertEquals(ids.size(), outboxDao.getOutboxSize());

        publishTrigger.publish();

        // Then
        var expectedIds = new HashSet<>(ids);
        var received = sampleEnvelopeConsumer.getEvents(100, e -> expectedIds.contains(e.sequenceNumber()), Duration.ofSeconds(20L))
                .stream()
                .sorted(Comparator.comparing(SampleEnvelope::sequenceNumber))
                .toList();

        assertEquals(messages.size(), received.size());

        IntStream.range(0, messages.size())
                .forEach(i -> assertEquals(messages.get(i), received.get(i)));

        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 0);
    }

    @Test
    void testPublishUsingBuilderFunctionShouldPublishWithGivenAttributes() {
        var ids = getUniqueIds(5);
        var messages = ids.stream().map(TestData::createEnvelope).toList();

        final String attributeKey = "builder";
        final String expectedAttributeValue = "function";
        var publishTrigger = runInTransaction(status -> target.persistEvents(messages, e -> PubsubMessage.newBuilder().putAttributes(attributeKey, expectedAttributeValue)));

        assertEquals(ids.size(), outboxDao.getOutboxSize());

        publishTrigger.publish();

        var expectedIds = new HashSet<>(ids);
        var receivedIds = sampleEnvelopeConsumer.getPubsubMessagesByMessagePredicate(5, e -> expectedAttributeValue.equals(e.getAttributesOrDefault(attributeKey, "FAILURE")), Duration.ofSeconds(5L))
                .stream()
                .map(message -> {
                    try {
                        return deserializer.read(message.getData().toStringUtf8());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(SampleEnvelope::sequenceNumber))
                .toList();

        assertEquals(expectedIds.size(), receivedIds.size());
        assertTrue(expectedIds.containsAll(receivedIds.stream().map(SampleEnvelope::sequenceNumber).collect(Collectors.toSet())));

        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 0);
    }

    @Test
    void verifyCorruptDataInDatabaseRemainsAndStillAllowsOtherMsgsToPublish() {
        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 0);
        OutboxEvent badOutBoxEvent = new OutboxEvent("something", 1, topicId, "{\"test\":\"test\"}");
        OutboxEvent goodOutBoxEvent = new OutboxEvent("somethingElse", 1, topicId, "{\"data\":\"eyJlbnRpdHlJZCI6IkpVTklUXzEiLCJzZXF1ZW5jZU51bWJlciI6MSwiYWN0aW9uIjoiRFVNTVkiLCJzYW1wbGVFdmVudCI6eyJmaWVsZCI6IlRFU1RfMSIsImFub3RoZXJGaWVsZCI6MX19\"}");

        insertInDatabase(badOutBoxEvent);
        insertInDatabase(goodOutBoxEvent);
        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 2);
        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 1);
        insertInDatabase(goodOutBoxEvent);
        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 2);
        await().atMost(Duration.ofSeconds(3)).until(() -> outboxDao.getOutboxSize() == 1);
        outboxDao.readOldInOutbox(Duration.ofSeconds(0), msg -> assertEquals("something", msg.id()), false, 10000L);
    }

    private void insertInDatabase(OutboxEvent badOutBoxEvent) {
        jdbcTemplate.update(INSERT_IN_OUTBOX,
                new MapSqlParameterSource()
                        .addValue("data", badOutBoxEvent.pubsubMessage())
                        .addValue("id", badOutBoxEvent.id())
                        .addValue("version", badOutBoxEvent.version())
                        .addValue("topicName", badOutBoxEvent.topicName())
        );
    }

    public static class SampleEnvelopeConsumer extends EventConsumer<SampleEnvelope> {
        public SampleEnvelopeConsumer() {
            super(SampleEnvelope.class);
        }
    }

    private <T> T runInTransaction(TransactionCallback<T> transactionCallback) {
        return new TransactionTemplate(transactionManager).execute(transactionCallback);
    }
}
