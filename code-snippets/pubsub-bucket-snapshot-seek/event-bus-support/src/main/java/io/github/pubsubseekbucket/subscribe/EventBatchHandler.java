package io.github.pubsubseekbucket.subscribe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;
import io.github.pubsubseekbucket.util.EventBusScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class EventBatchHandler<EventType> implements EventHandler<EventType> {
    private final int maxBatchSize;
    private final @NonNull EventHandler<List<EventType>> eventHandler;

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    EventBatchHandler(@NonNull EventHandler<List<EventType>> eventHandler, int maxBatchSize, Duration maxDelay, EventBusScheduler eventBusScheduler) {
        this.eventHandler = eventHandler;
        this.maxBatchSize = maxBatchSize;

        if (maxDelay.isPositive()) {
            if (maxBatchSize == 1) {
                throw new IllegalArgumentException("No use setting up max delay " + maxDelay + " when event batching is turned off by setting max batch size to 1");
            }
            if (eventBusScheduler == null) {
                throw new IllegalArgumentException("maxDelay is positive but no scheduler was provided");
            }
            PeriodicTrigger trigger = new PeriodicTrigger(maxDelay);
            eventBusScheduler.addTriggerTask(new TriggerTask(this::flush, trigger));
            log.info("Registered periodic trigger for BatchSubscriber with maxDelay={}", maxDelay);
        } else if (maxBatchSize > 1) {
            throw new IllegalArgumentException("Enabling event batching by setting max batch size to " + maxBatchSize + " requires a positive max delay, otherwise events may be delayed indefinitely.");
        }
    }

    private final List<Pair<EventType, CompletableFuture>> pendingEvents = new ArrayList<>();

    @Override
    public CompletableFuture<?> handle(EventType event) {
        if (maxBatchSize == 1) {
            return eventHandler.handle(List.of(event));
        } else {
            CompletableFuture<?> future = new CompletableFuture<>();
            List<Pair<EventType, CompletableFuture>> batchToSend = null;
            synchronized (this) {
                pendingEvents.add(Pair.of(event, future));

                if (pendingEvents.size() == maxBatchSize) {
                    batchToSend = new ArrayList<>(pendingEvents);
                    pendingEvents.clear();
                }
            }
            if (batchToSend != null) {
                sendBatch(batchToSend);
            }
            return future;
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void sendBatch(List<Pair<EventType, CompletableFuture>> events) {
        CompletableFuture<?> batchFuture;
        try {
            batchFuture = eventHandler.handle(events.stream().map(Pair::getLeft).toList());
        } catch (Exception e) {
            events.stream().map(Pair::getRight).forEach(f -> f.completeExceptionally(e));
            return;
        }
        batchFuture.whenComplete((a, e) -> {
            if (e != null) {
                events.stream().map(Pair::getRight).forEach(f -> f.completeExceptionally(e));
            } else {
                events.stream().map(Pair::getRight).forEach(f -> f.complete(a));
            }
        });
    }

    @Scheduled
    void flush() {
        List<Pair<EventType, CompletableFuture>> batchToSend = null;
        synchronized (this) {
            if (!pendingEvents.isEmpty()) {
                batchToSend = new ArrayList<>(pendingEvents);
                pendingEvents.clear();
            }
        }
        if (batchToSend != null) {
            sendBatch(batchToSend);
        }
    }
}
