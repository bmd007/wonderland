package io.github.pubsubseekbucket.subscribe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;
import io.github.pubsubseekbucket.util.EventBusScheduler;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EventBatchHandlerTest {
    @Mock
    EventHandler<List<String>> handler;

    @Mock
    EventBusScheduler eventBusScheduler;

    Random random = new Random();

    @Test
    public void callEventHandlerForEachFullBatch() {
        int batchSize = random.nextInt(10) + 5;
        int batches = random.nextInt(3) + 2;

        CompletableFuture<String> batchFuture = new CompletableFuture<>();
        doReturn(batchFuture).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, batchSize, Duration.ofSeconds(1), eventBusScheduler);

        var events = IntStream.range(0, batches * batchSize).mapToObj(i -> "event" + i).toList();
        events.forEach(target::handle);

        IntStream.range(0, batches).forEach(i -> verify(handler).handle(events.subList(i * batchSize, (i + 1) * batchSize)));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void callEventHandlerImmediatelyWhenBatchSizeIsOne() {
        CompletableFuture<String> batchFuture = new CompletableFuture<>();
        doReturn(batchFuture).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, 1, Duration.ZERO, eventBusScheduler);

        var future = target.handle("event");

        verify(handler).handle(List.of("event"));
        assertSame(batchFuture, future);
    }

    @Test
    public void ackAllEventsWhenBatchIsAcked() {
        int batchSize = random.nextInt(10) + 5;

        CompletableFuture<String> batchFuture = new CompletableFuture<>();
        doReturn(batchFuture).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, batchSize, Duration.ofSeconds(1), eventBusScheduler);

        var eventFutures = IntStream.range(0, batchSize)
                .mapToObj(i -> target.handle("event" + i))
                .toList();

        var completionValue = "foobar";
        batchFuture.complete(completionValue);

        eventFutures.forEach(f -> {
            assertTrue(f.isDone());
            assertFalse(f.isCompletedExceptionally());
            assertEquals(completionValue, f.getNow(null));
        });
    }

    @Test
    public void nackAllEventsWhenBatchIsCompletedExceptionally() {
        int batchSize = random.nextInt(10) + 5;

        CompletableFuture<String> batchFuture = new CompletableFuture<>();
        doReturn(batchFuture).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, batchSize, Duration.ofSeconds(1), eventBusScheduler);

        var eventFutures = IntStream.range(0, batchSize)
                .mapToObj(i -> target.handle("event" + i))
                .toList();

        RuntimeException exception = new RuntimeException("dummy");
        batchFuture.completeExceptionally(exception);

        eventFutures.forEach(f -> {
            assertTrue(f.isDone());
            assertTrue(f.isCompletedExceptionally());
            assertEquals(exception, f.exceptionNow());
        });
    }

    @Test
    public void nackAllEventsWhenBatchThrowsException() {
        int batchSize = random.nextInt(10) + 5;

        RuntimeException exception = new RuntimeException("dummy");
        doThrow(exception).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, batchSize, Duration.ofSeconds(1), eventBusScheduler);

        var eventFutures = IntStream.range(0, batchSize)
                .mapToObj(i -> target.handle("event" + i))
                .toList();

        eventFutures.forEach(f -> {
            assertTrue(f.isDone());
            assertTrue(f.isCompletedExceptionally());
            assertEquals(exception, f.exceptionNow());
        });
    }

    @Test
    public void callEventHandlerWhenFlushed() {
        int batchSize = 5;

        CompletableFuture<String> batchFuture = new CompletableFuture<>();
        doReturn(batchFuture).when(handler).handle(any());

        var target = new EventBatchHandler<>(handler, batchSize, Duration.ofSeconds(1), eventBusScheduler);

        var events = IntStream.range(0, 2).mapToObj(i -> "event" + i).toList();
        events.forEach(target::handle);
        verifyNoInteractions(handler);

        ArgumentCaptor<TriggerTask> captor = ArgumentCaptor.forClass(TriggerTask.class);
        verify(eventBusScheduler).addTriggerTask(captor.capture());

        captor.getValue().getRunnable().run();

        verify(handler).handle(events);
    }

    @Test
    public void scheduleFlush() {
        Duration maxDelay = Duration.ofSeconds(3);

        new EventBatchHandler<>(handler, 5, maxDelay, eventBusScheduler);

        ArgumentCaptor<TriggerTask> captor = ArgumentCaptor.forClass(TriggerTask.class);
        verify(eventBusScheduler).addTriggerTask(captor.capture());

        assertEquals(new PeriodicTrigger(maxDelay), captor.getValue().getTrigger());
    }

    @Test
    public void doNotScheduleFlushWhenMaxDelayIsZero() {
        new EventBatchHandler<>(handler, 1, Duration.ZERO, eventBusScheduler);

        verifyNoInteractions(eventBusScheduler);
    }

    @Test
    public void positiveDelayWhenBatchingIsOffFails() {
        assertThrows(IllegalArgumentException.class, () -> new EventBatchHandler<>(handler, 1, Duration.ofSeconds(10), eventBusScheduler));
    }

    @Test
    public void noDelayWhenBatchingIsOnFails() {
        assertThrows(IllegalArgumentException.class, () -> new EventBatchHandler<>(handler, 10, Duration.ZERO, eventBusScheduler));
    }

    @Test
    public void positiveDelayWithoutSchedulerFails() {
        assertThrows(IllegalArgumentException.class, () -> new EventBatchHandler<>(handler, 5, Duration.ofSeconds(10), null));
    }
}
