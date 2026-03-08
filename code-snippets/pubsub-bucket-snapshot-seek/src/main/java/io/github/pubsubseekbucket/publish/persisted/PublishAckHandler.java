package io.github.pubsubseekbucket.publish.persisted;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import io.github.pubsubseekbucket.util.EventBusScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class PublishAckHandler {
    private final OutboxDao outboxDao;
    private final int maxBatchSize;

    private final Collection<OutboxEvent> pendingEvents = new ArrayList<>();

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public PublishAckHandler(OutboxDao outboxDao, int maxBatchSize, Duration maxDelay, EventBusScheduler eventBusScheduler) {
        this.outboxDao = outboxDao;
        this.maxBatchSize = maxBatchSize;

        if (maxBatchSize < 1) {
            throw new IllegalArgumentException("maxBatchSize must be at least 1");
        }
        if (maxDelay.isNegative()) {
            throw new IllegalArgumentException("maxDelay must not be negative");
        }

        if (maxDelay.isPositive()) {
            if (maxBatchSize == 1) {
                throw new IllegalArgumentException("No use setting up max delay " + maxDelay + " when ack batching is turned off by setting max batch size to 1");
            }
            if (eventBusScheduler == null) {
                throw new IllegalArgumentException("maxDelay is positive but no scheduler was provided");
            }
            PeriodicTrigger trigger = new PeriodicTrigger(maxDelay);
            eventBusScheduler.addTriggerTask(new TriggerTask(this::flush, trigger));
            log.info("Registered periodic trigger for PublishAckHandler with maxDelay={}", maxDelay);
        } else if (maxBatchSize > 1) {
            throw new IllegalArgumentException("Enabling ack batching by setting max batch size to " + maxBatchSize + " requires a positive max delay, otherwise ack events may be delayed indefinitely.");
        }
    }

    public void handle(OutboxEvent outboxEvent) {
        if (maxBatchSize == 1) {
            outboxDao.deleteFromOutbox(outboxEvent);
        } else {
            Collection<OutboxEvent> batchToSend = null;

            synchronized (this) {
                pendingEvents.add(outboxEvent);

                if (pendingEvents.size() == maxBatchSize) {
                    batchToSend = new ArrayList<>(pendingEvents);
                    pendingEvents.clear();
                }
            }

            if (batchToSend != null) {
                deleteEventsFromOutbox(batchToSend);
            }
        }
    }

    private void flush() {
        Collection<OutboxEvent> batchToSend = null;

        synchronized (this) {
            if (!pendingEvents.isEmpty()) {
                batchToSend = new ArrayList<>(pendingEvents);
                pendingEvents.clear();
            }
        }

        if (batchToSend != null) {
            deleteEventsFromOutbox(batchToSend);
        }
    }

    private void deleteEventsFromOutbox(Collection<OutboxEvent> ackedEvents) {
        // Sort the events to ensure any database locks are taken in the same order so we avoid deadlocks.
        List<OutboxEvent> sortedEvents = ackedEvents.stream()
                .sorted(Comparator.comparing(OutboxEvent::topicName)
                        .thenComparing(OutboxEvent::id)
                        .thenComparing(OutboxEvent::version))
                .toList();

        outboxDao.deleteFromOutbox(sortedEvents);
    }
}
