package io.github.pubsubseekbucket.publish.persisted;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.NonNull;
import io.github.pubsubseekbucket.publish.Publisher;
import io.github.pubsubseekbucket.util.EventBusScheduler;
import io.github.pubsubseekbucket.util.Serializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractPersistedPublisherFactory {
    public static final int DEFAULT_MAX_BATCH_SIZE = 1;
    public static final Duration DEFAULT_MAX_DELAY = Duration.ZERO;

    private final PubSubTemplate pubSubTemplate;
    private final Optional<OutboxDao> outboxDao;
    private final ObjectMapper objectMapper;
    private final EventBusScheduler eventBusScheduler;
    private final PublishTriggerRepository publishTriggerRepository;

    private final Map<String, PersistedPublisher<?>> publisherByTopic = new HashMap<>();

    public AbstractPersistedPublisherFactory(PubSubTemplate pubSubTemplate, Optional<OutboxDao> outboxDao, ObjectMapper objectMapper, EventBusScheduler eventBusScheduler, PublishTriggerRepository publishTriggerRepository) {
        this.pubSubTemplate = pubSubTemplate;
        this.outboxDao = outboxDao;
        this.objectMapper = objectMapper;
        this.eventBusScheduler = eventBusScheduler;
        this.publishTriggerRepository = publishTriggerRepository;
    }

    public <T> Publisher<T> createPublisher(@NonNull String topicName,
                                            @NonNull Serializer<T, String> serializer) {
        return new Publisher<>(topicName, pubSubTemplate, serializer);
    }

    public <T> Publisher<T> createPublisher(String topicName) {
        return new Publisher<>(topicName, pubSubTemplate, getDefaultSerializer());
    }

    public <T> PersistedPublisher<T> createPersistedPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, getDefaultSerializer()), idExtractor, versionExtractor, getDefaultSerializer(), getOutboxDao(), DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_DELAY, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return publisher;
    }

    public <T> PersistedPublisher<T> createPersistedPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, serializer), idExtractor, versionExtractor, serializer, getOutboxDao(), DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_DELAY, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return publisher;
    }

    public <T> PersistedPublisher<T> createPersistedPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            int maxAckBatchSize,
            Duration maxAckDelay) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, getDefaultSerializer()), idExtractor, versionExtractor, getDefaultSerializer(), getOutboxDao(), maxAckBatchSize, maxAckDelay, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return publisher;
    }

    public <T> PersistedPublisher<T> createPersistedPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer,
            int maxAckBatchSize,
            Duration maxAckDelay) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, serializer), idExtractor, versionExtractor, serializer, getOutboxDao(), maxAckBatchSize, maxAckDelay, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return publisher;
    }

    public <T> OutboxPublisher<T> createOutboxPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, getDefaultSerializer()), idExtractor, versionExtractor, getDefaultSerializer(), getOutboxDao(), DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_DELAY, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return new OutboxPublisher<>(publisher, publishTriggerRepository);
    }

    public <T> OutboxPublisher<T> createOutboxPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, serializer), idExtractor, versionExtractor, serializer, getOutboxDao(), DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_DELAY, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return new OutboxPublisher<>(publisher, publishTriggerRepository);
    }

    public <T> OutboxPublisher<T> createOutboxPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            int maxAckBatchSize,
            Duration maxAckDelay) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, getDefaultSerializer()), idExtractor, versionExtractor, getDefaultSerializer(), getOutboxDao(), maxAckBatchSize, maxAckDelay, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return new OutboxPublisher<>(publisher, publishTriggerRepository);
    }

    public <T> OutboxPublisher<T> createOutboxPublisher(
            String topicName,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer,
            int maxAckBatchSize,
            Duration maxAckDelay) {
        PersistedPublisher<T> publisher = new PersistedPublisher<>(topicName, createPublisher(topicName, serializer), idExtractor, versionExtractor, serializer, getOutboxDao(), maxAckBatchSize, maxAckDelay, eventBusScheduler);
        registerPublisher(topicName, publisher);
        return new OutboxPublisher<>(publisher, publishTriggerRepository);
    }

    private <T> void registerPublisher(String topicName, PersistedPublisher<T> result) {
        if (publisherByTopic.containsKey(topicName)) {
            throw new IllegalArgumentException("Persisted publisher already exists for topic " + topicName);
        }
        publisherByTopic.put(topicName, result);
    }

    public Optional<PersistedPublisher<?>> getPublisher(String topicName) {
        return Optional.ofNullable(publisherByTopic.get(topicName));
    }

    private <T> Serializer<T, String> getDefaultSerializer() {
        return objectMapper::writeValueAsString;
    }

    private OutboxDao getOutboxDao() {
        return outboxDao.orElseThrow(() -> new IllegalStateException("Creating a persisted publisher without an outbox dao"));
    }
}
