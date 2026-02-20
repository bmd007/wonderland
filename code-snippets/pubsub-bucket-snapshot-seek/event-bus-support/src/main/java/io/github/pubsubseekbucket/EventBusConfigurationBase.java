package io.github.pubsubseekbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import io.github.pubsubseekbucket.publish.PublisherFactory;
import io.github.pubsubseekbucket.publish.persisted.PublishTriggerRepository;
import io.github.pubsubseekbucket.subscribe.SubscriberFactory;
import io.github.pubsubseekbucket.util.DynamicSubscriptionDeleter;
import io.github.pubsubseekbucket.util.EventBusScheduler;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import({SubscriberFactory.class, PublisherFactory.class, EventBusScheduler.class})
public class EventBusConfigurationBase {

    @Bean
    @Primary
    public PubSubMessageConverter pubSubMessageConverter(ObjectMapper objectMapper) {
        return new JacksonPubSubMessageConverter(objectMapper);
    }

    /**
     * set property: subscription.deleter.active: false
     * to not delete dynamic subscriptions when application closes down.
     * Useful in e2e tests etc, should not be used in deployed code
     */
    @Bean
    @ConditionalOnProperty(value = "subscription.deleter.active", havingValue = "true", matchIfMissing = true)
    public DynamicSubscriptionDeleter dynamicSubscriptionDeleter(SubscriptionAdminUtil subscriptionAdminUtil) {
        return new DynamicSubscriptionDeleter(subscriptionAdminUtil);
    }

    @Bean
    PublishTriggerRepository publishTriggerRepository() {
        return new PublishTriggerRepository();
    }
}
