package io.github.pubsubseekbucket.subscribe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import lombok.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import io.github.pubsubseekbucket.util.Deserializer;
import io.github.pubsubseekbucket.util.EventBusScheduler;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import java.time.Duration;
import java.util.List;

@Configuration
public class SubscriberFactory {
    private final PubSubTemplate pubSubTemplate;
    private final SubscriptionAdminUtil subscriptionAdminUtil;
    private final ObjectMapper objectMapper;
    private final EventBusScheduler eventBusScheduler;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SubscriberFactory(PubSubTemplate pubSubTemplate, SubscriptionAdminUtil subscriptionAdminUtil, ObjectMapper objectMapper, EventBusScheduler eventBusScheduler, ApplicationEventPublisher applicationEventPublisher) {
        this.pubSubTemplate = pubSubTemplate;
        this.subscriptionAdminUtil = subscriptionAdminUtil;
        this.objectMapper = objectMapper;
        this.eventBusScheduler = eventBusScheduler;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public <EventType> Subscriber<EventType> createSubscriber(
            @NonNull String topicName,
            @NonNull String subscriptionName,
            @NonNull EventHandler<EventType> eventHandler,
            @NonNull TypeReference<EventType> eventType) {
        return new Subscriber<>(topicName, subscriptionName, eventHandler, pubSubTemplate, getDefaultDeserializer(eventType), subscriptionAdminUtil, applicationEventPublisher);
    }

    public <EventType> Subscriber<EventType> createSubscriber(
            @NonNull String topicName,
            @NonNull String subscriptionName,
            @NonNull EventHandler<EventType> eventHandler,
            @NonNull Deserializer<PubsubMessage, EventType> deserializer) {
        return new Subscriber<>(topicName, subscriptionName, eventHandler, pubSubTemplate, deserializer, subscriptionAdminUtil, applicationEventPublisher);
    }

    public <EventType> Subscriber<EventType> createSubscriber(
            @NonNull Subscription subscription,
            @NonNull EventHandler<EventType> eventHandler,
            @NonNull TypeReference<EventType> eventType) {
        return new Subscriber<>(getTopicName(subscription), getSubscriptionName(subscription), eventHandler, pubSubTemplate, getDefaultDeserializer(eventType), subscriptionAdminUtil, applicationEventPublisher);
    }

    public <EventType> Subscriber<EventType> createSubscriber(
            @NonNull Subscription subscription,
            @NonNull EventHandler<EventType> eventHandler,
            @NonNull Deserializer<PubsubMessage, EventType> deserializer) {
        return new Subscriber<>(getTopicName(subscription), getSubscriptionName(subscription), eventHandler, pubSubTemplate, deserializer, subscriptionAdminUtil, applicationEventPublisher);
    }

    public <EventType> EventBatchHandler<EventType> createEventBatchHandler(@NonNull EventHandler<List<EventType>> eventHandler, int maxBatchSize, Duration maxDelay) {
        return new EventBatchHandler<>(eventHandler, maxBatchSize, maxDelay, eventBusScheduler);
    }

    private static String getTopicName(Subscription subscription) {
        return TopicName.parse(subscription.getTopic()).getTopic();
    }

    private static String getSubscriptionName(Subscription subscription) {
        return ProjectSubscriptionName.parse(subscription.getName()).getSubscription();
    }

    private <EventType> Deserializer<PubsubMessage, EventType> getDefaultDeserializer(TypeReference<EventType> eventType) {
        return msg -> objectMapper.readValue(msg.getData().toStringUtf8(), eventType);
    }
}

