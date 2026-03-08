package io.github.pubsubseekbucket.publish.persisted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;
import io.github.pubsubseekbucket.util.EventBusScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PublishAckHandlerTest {
    @Mock
    private OutboxDao outboxDao;
    @Mock
    private EventBusScheduler eventBusScheduler;

    private PublishAckHandler target;

    @Test
    void outboxNotCalledIfBatchIsNotFull() {
        int maxBatchSize = 10;
        target = new PublishAckHandler(outboxDao, maxBatchSize, Duration.ofSeconds(1), eventBusScheduler);

        IntStream.range(0, maxBatchSize - 1).forEach(i -> target.handle(new OutboxEvent("id_" + i, 1L, "topic", "payload")));

        verify(outboxDao, never()).deleteFromOutbox(any(OutboxEvent.class));
        verify(outboxDao, never()).deleteFromOutbox(any(Collection.class));
    }

    @Test
    void deleteFromOutboxWhenBatchIsFull() {
        int maxBatchSize = 5;
        target = new PublishAckHandler(outboxDao, maxBatchSize, Duration.ofSeconds(1), eventBusScheduler);

        var events = IntStream.range(0, maxBatchSize)
                .mapToObj(i -> {
                    OutboxEvent outboxEvent = new OutboxEvent("id_" + i, 1L, "topic", "payload");
                    target.handle(outboxEvent);
                    return outboxEvent;
                })
                .toList();

        verify(outboxDao).deleteFromOutbox(events);
    }

    @Test
    void batchToAckIsSorted() {
        int maxBatchSize = 30;
        target = new PublishAckHandler(outboxDao, maxBatchSize, Duration.ofSeconds(1), eventBusScheduler);

        var events = new ArrayList<>(IntStream.range(0, maxBatchSize)
                .mapToObj(i -> new OutboxEvent("id_" + i % 5, i % 3, "topic_" + i % 2, "payload"))
                .toList());

        Collections.shuffle(events);
        events.forEach(target::handle);

        var expectedEvents = events.stream()
                .sorted(Comparator.comparing(OutboxEvent::topicName)
                        .thenComparing(OutboxEvent::id)
                        .thenComparing(OutboxEvent::version))
                .toList();
        verify(outboxDao).deleteFromOutbox(expectedEvents);
    }

    @Test
    void deleteImmediatelyFromOutboxIfMaxBatchSizeIsOne() {
        target = new PublishAckHandler(outboxDao, 1, Duration.ZERO, eventBusScheduler);

        OutboxEvent outboxEvent = new OutboxEvent("id", 1L, "topic", "payload");
        target.handle(outboxEvent);

        verify(outboxDao).deleteFromOutbox(outboxEvent);
    }

    @Test
    public void scheduleFlush() {
        Duration maxDelay = Duration.ofSeconds(3);

        target = new PublishAckHandler(outboxDao, 5, maxDelay, eventBusScheduler);

        ArgumentCaptor<TriggerTask> captor = ArgumentCaptor.forClass(TriggerTask.class);
        verify(eventBusScheduler).addTriggerTask(captor.capture());
        assertEquals(maxDelay, ((PeriodicTrigger) captor.getValue().getTrigger()).getPeriodDuration());
        assertNotNull(captor.getValue().getTrigger());
    }

    @Test
    public void doNotScheduleFlushWhenMaxDelayIsZero() {
        Duration maxDelay = Duration.ofSeconds(0);

        target = new PublishAckHandler(outboxDao, 1, maxDelay, eventBusScheduler);

        verifyNoInteractions(eventBusScheduler);
    }

    @Test
    public void positiveDelayWhenBatchingIsOffFails() {
        assertThrows(IllegalArgumentException.class, () -> new PublishAckHandler(outboxDao, 1, Duration.ofSeconds(10), eventBusScheduler));
    }

    @Test
    public void noDelayWhenBatchingIsOnFails() {
        assertThrows(IllegalArgumentException.class, () -> new PublishAckHandler(outboxDao, 10, Duration.ZERO, eventBusScheduler));
    }

    @Test
    public void positiveDelayWithoutSchedulerFails() {
        assertThrows(IllegalArgumentException.class, () -> new PublishAckHandler(outboxDao, 5, Duration.ofSeconds(10), null));
    }
}
