package io.github.pubsubseekbucket.publish.persisted;

import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import io.github.pubsubseekbucket.publish.Publisher;
import io.github.pubsubseekbucket.subscribe.integrationtest.Application;
import io.github.pubsubseekbucket.util.Deserializer;
import se.bmd.trading.testsupport.metrics.MetricsAccess;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static io.github.pubsubseekbucket.publish.persisted.TestData.getUniqueIds;
import static io.github.pubsubseekbucket.publish.persisted.TestData.nextUniqueId;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class, PersistedPublisherConfiguration.class, PersistedPublisherTestConfig.class},
        properties = {
                "MY_POD_NAME=TEST_POD",
                "eventbus.publish.republishThresholdInSeconds=0",
                "eventbus.publish.drainFrequencyInSeconds=1",
                "management.endpoints.web.exposure.include=metrics",
                "management.endpoint.metrics.enabled=true"
        })
class PersistedPublisherFailureTest {

    @MockBean
    Publisher<TestData.SampleEnvelope> mockPublisher;

    @Autowired
    OutboxDao eventOutboxDao;

    String testName;

    @Autowired
    PersistedPublisher<TestData.SampleEnvelope> target;

    @Captor
    ArgumentCaptor<PubsubMessage> mockPublisherCaptor;

    @Autowired
    Deserializer<String, TestData.SampleEnvelope> deserializer;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @LocalManagementPort
    int managementPort;
    @Autowired
    PlatformTransactionManager transactionManager;
    MetricsAccess metricsAccess;

    @BeforeEach
    protected void beforeEach(TestInfo testInfo) {

        jdbcTemplate.execute("delete from outbox");

        testName = testInfo.getTestClass().get().getSimpleName() + "_" + testInfo.getTestMethod().get().getName();

        metricsAccess = new MetricsAccess(managementPort);

        log.info("================================================================");
        log.info("Starting test {}", testName);
        log.info("----------------------------------------------------------------");
    }

    @Test
    void scheduledOutboxFunctionalityShouldPublishIfInitialPublishFails() throws IOException {

        // Given
        var outgoingEnvelope = TestData.createEnvelope(nextUniqueId());

        when(mockPublisher.publish(any(PubsubMessage.class))).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("Mocked publish(..) failure")),
                CompletableFuture.completedFuture("OK"));

        long republishCountBefore = metricsAccess.getMetricValue("republished_events");

        // When
        var publishTrigger = runInTransaction(status -> target.persistEvent(outgoingEnvelope));

        publishTrigger.publish();

        await().atMost(Duration.ofSeconds(5)).until(() -> (metricsAccess.getMetricValue("republished_events") - republishCountBefore) > 0);

        // Then
        verify(mockPublisher, times(2)).publish(mockPublisherCaptor.capture());

        assertEquals(2, mockPublisherCaptor.getAllValues().size());
        var rejectedPubsubMessage = mockPublisherCaptor.getAllValues().get(0);
        var publishedPubsubMessage = mockPublisherCaptor.getAllValues().get(1);

        assertEquals(rejectedPubsubMessage.getData(), publishedPubsubMessage.getData());

        assertEquals("NOT_SET", rejectedPubsubMessage.getAttributesOrDefault("outbox_resend", "NOT_SET"));
        assertEquals("true", publishedPubsubMessage.getAttributesOrThrow("outbox_resend"));

        assertEquals(0, eventOutboxDao.getOutboxSize());

        var receivedEnvelope = deserializer.read(publishedPubsubMessage.getData().toStringUtf8());

        assertEquals(outgoingEnvelope, receivedEnvelope);
    }

    @Test
    void publishManyReturnSuccessAndPublishFromOutboxIfFailedToPublish() {

        // Given
        var ids = getUniqueIds(20);
        var messages = ids.stream().map(TestData::createEnvelope).toList();

        when(mockPublisher.publish(any(PubsubMessage.class))).thenReturn(
                CompletableFuture.completedFuture("OK"),
                CompletableFuture.failedFuture(new RuntimeException("Mocked publish(..) failure")),
                CompletableFuture.failedFuture(new RuntimeException("Mocked publish(..) failure")),
                CompletableFuture.completedFuture("OK"));

        long republishCountBefore = metricsAccess.getMetricValue("republished_events");

        // When
        var publishTrigger = runInTransaction(status -> target.persistEvents(messages));

        assertEquals(ids.size(), eventOutboxDao.getOutboxSize());

        publishTrigger.publish();

        await().atMost(Duration.ofSeconds(5)).until(() -> (metricsAccess.getMetricValue("republished_events") - republishCountBefore) > 0);

        // Then
        verify(mockPublisher, times(22)).publish(mockPublisherCaptor.capture());

        final List<PubsubMessage> capturedValues = mockPublisherCaptor.getAllValues();
        assertEquals(22, capturedValues.size());
        final ConcurrentMap<String, Long> publishPerEntityId = capturedValues.stream()
                .collect(Collectors.groupingByConcurrent(
                        publishedMsg -> {
                            try {
                                return deserializer.read(publishedMsg.getData().toStringUtf8()).entityId();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Collectors.counting()));

        final int messagesPublishedTwice = publishPerEntityId.values().stream()
                .filter(publishCount -> publishCount == 2)
                .toList().size();
        final int messagesPublishedOnce = publishPerEntityId.values().stream()
                .filter(publishCount -> publishCount == 1)
                .toList().size();

        assertEquals(2, messagesPublishedTwice);
        assertEquals(18, messagesPublishedOnce);

        assertEquals(0, eventOutboxDao.getOutboxSize());
    }

    @Test
    void scheduledOutboxFunctionalityShouldPublishWithAttributesIfInitialPublishFails() throws IOException {
        // Given
        var ids = getUniqueIds(1);
        var messageToPublish = ids.stream().map(TestData::createEnvelope).toList();

        final String attributeKey = "builder";
        final String expectedAttributeValue = "function";

        when(mockPublisher.publish(any(PubsubMessage.class))).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("Mocked publish(..) failure")),
                CompletableFuture.completedFuture("OK"));

        long republishCountBefore = metricsAccess.getMetricValue("republished_events");

        // When
        var publishTrigger = runInTransaction(status -> target.persistEvents(messageToPublish, e -> PubsubMessage.newBuilder().putAttributes(attributeKey, expectedAttributeValue)));

        assertEquals(ids.size(), eventOutboxDao.getOutboxSize());

        publishTrigger.publish();

        await().atMost(Duration.ofSeconds(5)).until(() -> (metricsAccess.getMetricValue("republished_events") - republishCountBefore) > 0);

        // Then
        verify(mockPublisher, times(2)).publish(mockPublisherCaptor.capture());

        final List<PubsubMessage> captoredPublishRequests = mockPublisherCaptor.getAllValues();
        assertEquals(2, captoredPublishRequests.size());
        var rejectedPubsubMessage = captoredPublishRequests.get(0);
        var publishedPubsubMessage = captoredPublishRequests.get(1);

        assertEquals(rejectedPubsubMessage.getData(), publishedPubsubMessage.getData());

        assertEquals("NOT_SET", rejectedPubsubMessage.getAttributesOrDefault("outbox_resend", "NOT_SET"));
        assertEquals("true", publishedPubsubMessage.getAttributesOrThrow("outbox_resend"));

        assertEquals(expectedAttributeValue, rejectedPubsubMessage.getAttributesOrDefault(attributeKey, "NOT_SET"));
        assertEquals(expectedAttributeValue, publishedPubsubMessage.getAttributesOrDefault(attributeKey, "NOT_SET"));

        assertEquals(0, eventOutboxDao.getOutboxSize());

        var receivedEnvelope = deserializer.read(publishedPubsubMessage.getData().toStringUtf8());

        assertEquals(messageToPublish.get(0), receivedEnvelope);
    }

    private <T> T runInTransaction(TransactionCallback<T> transactionCallback) {
        return new TransactionTemplate(transactionManager).execute(transactionCallback);
    }
}
