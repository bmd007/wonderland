package io.github.pubsubseekbucket.publish.persisted;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Slf4j
abstract class OutboxDaoTest {
    @Autowired
    protected OutboxDao target;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PlatformTransactionManager transactionManager;

    private final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

    @Test
    void insertInOutbox() {
        // Given
        long before = target.getOutboxSize();

        // When
        var outbox = new OutboxEvent("TS_ID_20230912", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(outbox));

        // Then
        assertEquals(before + 1, target.getOutboxSize());
        Consumer<OutboxEvent> consumer = mock(Consumer.class);
        target.readOldInOutbox(Duration.ZERO, consumer, false, 10000L);

        OutboxEvent expectedEvent = new OutboxEvent(outbox.id(), outbox.version(), "topic1", outbox.pubsubMessage());
        verify(consumer).accept(expectedEvent);
    }

    @Test
    void readOldFromOutbox() throws InterruptedException {
        // Given
        var oldEvent = new OutboxEvent("TS_ID_20230912", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent));

        Thread.sleep(1000);

        var newEvent = new OutboxEvent("TS_ID_20230913", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(newEvent));

        // When
        List<OutboxEvent> oldEvents = new ArrayList<>();
        target.readOldInOutbox(Duration.of(500, ChronoUnit.MILLIS), oldEvents::add, false, 10000L);

        // Then
        assertEquals(List.of(oldEvent), oldEvents);
    }

    @Test
    void readLimitedNumberOfOldEventsFromOutbox() throws InterruptedException {
        // Given
        var oldEvent1 = new OutboxEvent("TS_ID_20230910", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent1));
        var oldEvent2 = new OutboxEvent("TS_ID_20230911", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent2));
        var oldEvent3 = new OutboxEvent("TS_ID_20230912", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(oldEvent3));

        Thread.sleep(1000);

        var newEvent = new OutboxEvent("TS_ID_20230913", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(newEvent));

        // When
        Set<OutboxEvent> oldEvents = new HashSet<>();
        target.readOldInOutbox(Duration.of(500, ChronoUnit.MILLIS), oldEvents::add, false, 2L);

        // Then
        assertEquals(2, oldEvents.size());
        assertEquals(Set.of(oldEvent1, oldEvent2), oldEvents);
    }

    protected void runInTransaction(TransactionCallback<Boolean> transactionCallback) {
        new TransactionTemplate(transactionManager).execute(transactionCallback);
    }

    @Test
    void insertMultipleInOutbox() {
        // Given
        long before = target.getOutboxSize();

        // When
        var events = IntStream.range(0, 10).mapToObj(i -> new OutboxEvent("ID_" + i, i, null, createDummyMessage(i))).toList();
        runInTransaction(status -> target.insertInOutbox(events, "topic2"));

        // Then
        assertEquals(before + events.size(), target.getOutboxSize());

        Consumer<OutboxEvent> consumer = mock(Consumer.class);
        target.readOldInOutbox(Duration.ZERO, consumer, false, 10000L);

        events.forEach(event -> {
            OutboxEvent expectedEvent = new OutboxEvent(event.id(), event.version(), "topic2", event.pubsubMessage());
            verify(consumer).accept(expectedEvent);
        });
    }

    @Test
    public void deleteFromOutbox() {
        // Given
        long before = target.getOutboxSize();

        // When
        var outbox = new OutboxEvent("TS_ID_20230912", 1, "topic1", createDummyMessage(1));
        runInTransaction(status -> target.insertInOutbox(outbox));
        target.deleteFromOutbox(outbox);

        // Then
        assertEquals(before, target.getOutboxSize());
    }

    @Test
    public void deleteMultipleFromOutbox() {
        // Given
        long before = target.getOutboxSize();

        // When
        int count = 10;
        var events = IntStream.range(0, count)
                .mapToObj(i -> {
                    var event = new OutboxEvent("TS_ID_" + i, i, "topic1", createDummyMessage(i));
                    runInTransaction(status -> target.insertInOutbox(event));
                    return event;
                })
                .toList();

        target.deleteFromOutbox(events);

        // Then
        assertEquals(before, target.getOutboxSize());
    }

    @NotNull
    protected String createDummyMessage(int payloadId) {
        try {
            final PubsubMessage.Builder builder = PubsubMessage.newBuilder();
            builder.putAttributes("test", "value");
            return printer.print(builder.setData(ByteString.copyFromUtf8("DUMMY MESSAGE " + payloadId)).build());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
