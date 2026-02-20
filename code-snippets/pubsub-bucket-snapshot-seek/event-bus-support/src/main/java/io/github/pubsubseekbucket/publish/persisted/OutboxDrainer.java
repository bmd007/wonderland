package io.github.pubsubseekbucket.publish.persisted;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import io.github.pubsubseekbucket.publish.PublisherFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class OutboxDrainer implements SchedulingConfigurer, SmartLifecycle {
    private final OutboxDao outboxDao;
    private final PublisherFactory publisherFactory;
    private final int drainFrequencyInSeconds;
    private final long drainLimit;
    private final boolean permitRandomOrder;
    private final int republishThresholdInSeconds;
    private final Counter republishedMessagesCounter;
    private final Map<String, PersistedPublisher<?>> publisherBeansByTopic;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public OutboxDrainer(OutboxDao outboxDao,
                         PublisherFactory publisherFactory,
                         List<PersistedPublisher<?>> persistedPublishers,
                         @Value("${eventbus.publish.drainFrequencyInSeconds:10}") int drainFrequencyInSeconds,
                         @Value("${eventbus.publish.drainLimit:10000}") long drainLimit,
                         @Value("${eventbus.publish.outbox.permitRandomOrder:false}") boolean permitRandomOrder,
                         @Value("${eventbus.publish.republishThresholdInSeconds:5}") int republishThresholdInSeconds, MeterRegistry meterRegistry) {
        this.outboxDao = outboxDao;
        this.publisherFactory = publisherFactory;
        this.drainFrequencyInSeconds = drainFrequencyInSeconds;
        this.drainLimit = drainLimit;
        this.permitRandomOrder = permitRandomOrder;
        this.republishThresholdInSeconds = republishThresholdInSeconds;

        // TODO: This code should not be necessary once all publishers are created by the factory
        publisherBeansByTopic = persistedPublishers.stream()
                .collect(Collectors.toMap(PersistedPublisher::getTopicName, Function.identity()));

        // Same counter name as in EventOutboxDrainer, for dashboard compatibility
        republishedMessagesCounter = meterRegistry.counter("republished_events");

        meterRegistry.gauge("outbox_size", this, obj -> outboxDao.getOutboxSize());
    }

    @SuppressFBWarnings({"DMI_RANDOM_USED_ONLY_ONCE", "PREDICTABLE_RANDOM"})
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofSeconds(drainFrequencyInSeconds));
        trigger.setInitialDelay(Duration.ofSeconds(new Random().nextInt(drainFrequencyInSeconds)));
        taskRegistrar.addTriggerTask(this::republishFromOutbox, trigger);
    }

    private void republishFromOutbox() {
        synchronized (this) {

            if (!running.get()) {
                log.debug("Skip republish as instance is not running, presumably during shutdown sequence");
                return;
            }

            var previousEventMessageCount = republishedMessagesCounter.count();
            outboxDao.readOldInOutbox(
                    Duration.ofSeconds(republishThresholdInSeconds),
                    oldEvent -> {
                        getPublisher(oldEvent.topicName())
                                .orElseThrow(() -> new IllegalArgumentException("Publisher for topic " + oldEvent.topicName() + " is missing."))
                                .rePublish(oldEvent);
                        republishedMessagesCounter.increment();
                    },
                    permitRandomOrder,
                    drainLimit);
            var newEventMessageCount = republishedMessagesCounter.count() - previousEventMessageCount;
            if (newEventMessageCount > 0L) {
                log.warn("Published {} event messages ", (long) newEventMessageCount);
            }
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public Optional<PersistedPublisher<?>> getPublisher(String topicName) {
        var publisherFromFactory = publisherFactory.getPublisher(topicName);
        var publisherBean = publisherBeansByTopic.get(topicName);

        if (publisherBean != null && publisherFromFactory.map(p -> p != publisherBean).orElse(false)) {
            throw new IllegalStateException("Publisher for topic " + topicName + " is defined both as a bean and in the factory");
        }
        return publisherFromFactory.or(() -> Optional.ofNullable(publisherBean));
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
