package io.github.pubsubseekbucket.publish.persisted;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import io.github.pubsubseekbucket.publish.Publisher;
import io.github.pubsubseekbucket.publish.persisted.TestData.SampleEnvelope;
import io.github.pubsubseekbucket.util.Deserializer;

@TestConfiguration
public class PersistedPublisherTestConfig {

    @Bean
    public Publisher<SampleEnvelope> testPublisher(
            @Value("${embedded.google.pubsub.topics-and-subscriptions[0].topic}") String topicName,
            PubSubTemplate pubSubTemplate,
            ObjectMapper objectMapper) {

        return new Publisher<>(topicName, pubSubTemplate, objectMapper);
    }

    @Bean
    public PersistedPublisher<SampleEnvelope> persistedPublisher(
            @Value("${embedded.google.pubsub.topics-and-subscriptions[0].topic}") String topicId,
            Publisher<SampleEnvelope> publisher,
            ObjectMapper objectMapper,
            OutboxDao eventOutboxDao) {

        return new PersistedPublisher<>(topicId,
                publisher,
                SampleEnvelope::entityId,
                SampleEnvelope::sequenceNumber,
                objectMapper::writeValueAsString,
                eventOutboxDao);
    }

    @Bean
    public Deserializer<String, SampleEnvelope> deserializer(ObjectMapper objectMapper) {
        return s -> objectMapper.readValue(s, SampleEnvelope.class);
    }
}
