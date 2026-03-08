package io.github.pubsubseekbucket.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Configuration;
import io.github.pubsubseekbucket.publish.persisted.AbstractPersistedPublisherFactory;
import io.github.pubsubseekbucket.publish.persisted.OutboxDao;
import io.github.pubsubseekbucket.publish.persisted.PublishTriggerRepository;
import io.github.pubsubseekbucket.util.EventBusScheduler;

import java.util.Optional;

@Configuration
public class PublisherFactory extends AbstractPersistedPublisherFactory {

    public PublisherFactory(PubSubTemplate pubSubTemplate, Optional<OutboxDao> outboxDao, ObjectMapper objectMapper, EventBusScheduler eventBusScheduler, PublishTriggerRepository publishTriggerRepository) {
        super(pubSubTemplate, outboxDao, objectMapper, eventBusScheduler, publishTriggerRepository);
    }

}
