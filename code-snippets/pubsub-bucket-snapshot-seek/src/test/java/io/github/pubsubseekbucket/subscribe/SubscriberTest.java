package io.github.pubsubseekbucket.subscribe;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Snapshot;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriberTest {
    private final String subscriptionName = "subscriptionName";
    ObjectMapper objectMapper = new ObjectMapper();


    @Mock
    PubSubTemplate pubSubTemplate;

    @Mock
    com.google.cloud.pubsub.v1.Subscriber pubsubTemplateSubscriber;

    @Mock
    EventHandler<Payload> eventHandler;

    @Mock
    BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    Subscriber<Payload> target;

    @Mock
    SubscriptionAdminUtil subscriptionAdminUtil;

    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @BeforeEach
    public void setup() {
        target = new Subscriber<>("topic name", subscriptionName, new TypeReference<>() {
        }, eventHandler, pubSubTemplate, objectMapper, subscriptionAdminUtil, applicationEventPublisher);
        lenient().when(pubSubTemplate.subscribe(any(), any())).thenReturn(pubsubTemplateSubscriber);

        Logger loggerSubscriber = (Logger) LoggerFactory.getLogger(Subscriber.class);
        loggerSubscriber.addAppender(mockedAppender);
    }

    @Test
    public void successfulParsingAndHandling() throws JsonProcessingException {
        // Given
        var payload = new Payload(17, "foo");

        var message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        when(eventHandler.handle(payload)).thenReturn(CompletableFuture.completedFuture(null));

        // When
        target.subscribe();
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(pubSubTemplate).subscribe(any(), any());
        verify(pubsubTemplateSubscriber).addListener(any(), any());
        verify(eventHandler).handle(payload);
        verify(basicAcknowledgeablePubsubMessage, times(1)).ack();
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);
    }

    @Test
    public void logErrorOnlyWhenMaxDeliveryAttemptsReached() throws JsonProcessingException {
        // Given
        var maxDeliveryAttempts = 3;

        var payload = new Payload(17, "foo");
        var message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        when(subscriptionAdminUtil.getMaxDeliveryAttempts(any())).thenReturn(OptionalInt.of(maxDeliveryAttempts));

        target.subscribe();

        // When
        when(eventHandler.handle(any())).thenThrow(IllegalArgumentException.class);
        target.setDeliveryAttemptFunction(event -> 1);
        target.receiveMessage(basicAcknowledgeablePubsubMessage);
        target.setDeliveryAttemptFunction(event -> 2);
        target.receiveMessage(basicAcknowledgeablePubsubMessage);
        target.setDeliveryAttemptFunction(event -> 3);
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        verify(mockedAppender, atLeast(3)).doAppend(loggingEventCaptor.capture());
        var loggingEvents = loggingEventCaptor.getAllValues().stream().filter(
                loggingEvent -> loggingEvent.getMessage().startsWith("Failed to handle message with id")
        ).toList();

        // Then
        verify(eventHandler, times(3)).handle(payload);
        verify(basicAcknowledgeablePubsubMessage, times(3)).nack();
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);

        assertEquals(3, loggingEvents.size());
        verifyLogEntry(loggingEvents.get(0), Level.INFO, 1, 3);
        verifyLogEntry(loggingEvents.get(1), Level.INFO, 2, 3);
        verifyLogEntry(loggingEvents.get(2), Level.ERROR, 3, 3);
    }

    @Test
    public void logWarnForEventsLackingDeliveryAttempt() throws JsonProcessingException {
        // Given
        var payload = new Payload(17, "foo");
        var message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        when(subscriptionAdminUtil.getMaxDeliveryAttempts(any())).thenReturn(OptionalInt.empty());

        target.subscribe();

        // When
        when(eventHandler.handle(any())).thenThrow(IllegalArgumentException.class);
        target.setDeliveryAttemptFunction(event -> null);
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(mockedAppender, atLeastOnce()).doAppend(loggingEventCaptor.capture());
        var loggingEvents = loggingEventCaptor.getAllValues().stream().filter(
                loggingEvent -> loggingEvent.getMessage().startsWith("Failed to handle message with id")
        ).toList();

        verify(eventHandler).handle(payload);
        verify(basicAcknowledgeablePubsubMessage).nack();
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);

        assertEquals(1, loggingEvents.size());
        verifyLogEntry(loggingEvents.get(0), Level.WARN);
    }

    private void verifyLogEntry(LoggingEvent loggingEvent, Level level, int attempt, int maxAttempts) {
        assertEquals(level, loggingEvent.getLevel());
        assertEquals(attempt, loggingEvent.getArgumentArray()[2]);
        assertEquals(maxAttempts, loggingEvent.getArgumentArray()[3]);
    }

    private void verifyLogEntry(LoggingEvent loggingEvent, Level level) {
        assertEquals(level, loggingEvent.getLevel());
        assertEquals(3, loggingEvent.getArgumentArray().length);
    }

    @Test
    public void failedParsing() throws InvalidProtocolBufferException {
        // Given
        var message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8("banana"))
                .build();

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(basicAcknowledgeablePubsubMessage).nack();
        verifyNoInteractions(eventHandler);
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);
    }

    @Test
    public void logErrorWhenParsingFails() throws InvalidProtocolBufferException {
        // Given
        var message = PubsubMessage.newBuilder()
                .setMessageId("foobar")
                .setData(ByteString.copyFromUtf8("banana"))
                .setPublishTime(Timestamp.newBuilder().setSeconds(354504L).build())
                .build();
        int attempt = 7;
        target.setDeliveryAttemptFunction(event -> attempt);
        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(mockedAppender).doAppend(loggingEventCaptor.capture());
        var loggingEvent = loggingEventCaptor.getValue();

        assertEquals("Failed to parse message with id {} published at {}, attempt {}, error: {}, payload: {}", loggingEvent.getMessage());
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        Object[] expectedParameters = {
                message.getMessageId(),
                Instant.ofEpochSecond(message.getPublishTime().getSeconds()),
                attempt,
                "Unrecognized token 'banana': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 7]",
                "banana"
        };
        assertArrayEquals(expectedParameters, loggingEvent.getArgumentArray());
    }

    @Test
    public void truncatePayloadWhenLoggingParseError() throws InvalidProtocolBufferException {
        // Given
        var message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(getLargeMessage()))
                .build();
        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(mockedAppender).doAppend(loggingEventCaptor.capture());
        var loggingEvent = loggingEventCaptor.getValue();

        assertEquals(message.getData().toStringUtf8().substring(0, 4000) + "...", loggingEvent.getArgumentArray()[4]);
    }

    private @NotNull String getLargeMessage() {
        return IntStream.range(0, 1000).mapToObj(i -> UUID.randomUUID()).map(Object::toString).collect(Collectors.joining());
    }

    @Test
    public void failedSynchronousHandling() throws JsonProcessingException {
        // Given
        var payload = new Payload(17, "foo");

        var message = PubsubMessage.newBuilder()
                .setMessageId("3")
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        when(eventHandler.handle(payload)).thenThrow(new RuntimeException());

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(eventHandler).handle(payload);
        verify(basicAcknowledgeablePubsubMessage).nack();
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);
    }

    @Test
    public void logFailedSynchronousHandling() throws JsonProcessingException {
        // Given
        var payload = new Payload(17, "foo");

        var message = PubsubMessage.newBuilder()
                .setMessageId("3")
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .setPublishTime(Timestamp.newBuilder().setSeconds(354504L).build())
                .build();

        int attempt = 7;
        int maxAttempts = 10;
        target.setDeliveryAttemptFunction(event -> attempt);
        when(subscriptionAdminUtil.getMaxDeliveryAttempts(any())).thenReturn(OptionalInt.of(maxAttempts));

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);
        when(eventHandler.handle(payload)).thenThrow(new RuntimeException());
        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        target.subscribe();
        reset(mockedAppender);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(mockedAppender).doAppend(loggingEventCaptor.capture());
        var loggingEvent = loggingEventCaptor.getValue();

        assertEquals("Failed to handle message with id {} published at {}, attempt {} of {}, event: {}", loggingEvent.getMessage());
        assertEquals(Level.INFO, loggingEvent.getLevel());
        Object[] expectedParameters = {
                message.getMessageId(),
                Instant.ofEpochSecond(message.getPublishTime().getSeconds()),
                attempt,
                maxAttempts,
                payload.toString()
        };
        assertArrayEquals(expectedParameters, loggingEvent.getArgumentArray());
    }

    @Test
    public void truncatePayloadWhenLoggingFailedHandling() throws JsonProcessingException {
        // Given
        var payload = new Payload(23465, getLargeMessage());
        var message = PubsubMessage.newBuilder()
                .setMessageId("3")
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        int attempt = 7;
        int maxAttempts = 10;
        target.setDeliveryAttemptFunction(event -> attempt);
        when(subscriptionAdminUtil.getMaxDeliveryAttempts(any())).thenReturn(OptionalInt.of(maxAttempts));

        when(eventHandler.handle(payload)).thenThrow(new RuntimeException());

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        target.subscribe();
        reset(mockedAppender);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(mockedAppender).doAppend(loggingEventCaptor.capture());
        var loggingEvent = loggingEventCaptor.getValue();

        assertEquals(payload.toString().substring(0, 4000) + "...", loggingEvent.getArgumentArray()[4]);
    }

    @Test
    public void failedAsynchronousHandling() throws JsonProcessingException {
        // Given
        var payload = new Payload(17, "foo");

        var message = PubsubMessage.newBuilder()
                .setMessageId("3")
                .setData(ByteString.copyFromUtf8(objectMapper.writeValueAsString(payload)))
                .build();

        when(eventHandler.handle(payload)).thenReturn(CompletableFuture.failedFuture(new RuntimeException()));

        when(basicAcknowledgeablePubsubMessage.getPubsubMessage()).thenReturn(message);

        // When
        target.receiveMessage(basicAcknowledgeablePubsubMessage);

        // Then
        verify(eventHandler).handle(payload);
        verify(basicAcknowledgeablePubsubMessage).nack();
        verifyNoMoreInteractions(basicAcknowledgeablePubsubMessage);
    }

    @Test
    public void shouldSeekToTimestamp() {
        // Given
        reset(pubSubTemplate);

        // When
        Instant timestamp = Instant.now();
        target.seekToTimestamp(timestamp);

        // Then
        verify(subscriptionAdminUtil).seekToTimestamp(subscriptionName, timestamp);
    }

    @Test
    public void shouldSeekToSnapshot() {
        // Given
        reset(pubSubTemplate);
        var snapshotId = "snapshotId";
        var topic = "topic name";
        var snapshot = Snapshot.newBuilder()
                .setName(snapshotId)
                .build();
        when(subscriptionAdminUtil.listSnapshots(topic)).thenReturn(List.of(snapshot));

        // When
        target.seekToSnapshot(snapshotId);

        // Then
        verify(subscriptionAdminUtil).seekToSnapshot(subscriptionName, snapshotId);
    }

    @Test
    public void shouldFailSeekToSnapshot() {
        // Given
        reset(pubSubTemplate);
        var snapshotId = "snapshotId";
        var topic = "topic name";
        var snapshot = Snapshot.newBuilder()
                .setName("flirp")
                .build();
        when(subscriptionAdminUtil.listSnapshots(topic)).thenReturn(List.of(snapshot));

        // When
        target.seekToSnapshot(snapshotId);

        // Then
        verify(subscriptionAdminUtil, times(0)).seekToSnapshot(subscriptionName, snapshotId);
    }

    @Value
    @Builder(toBuilder = true)
    @With
    @Jacksonized
    private static class Payload {
        int number;
        String name;
    }
}
