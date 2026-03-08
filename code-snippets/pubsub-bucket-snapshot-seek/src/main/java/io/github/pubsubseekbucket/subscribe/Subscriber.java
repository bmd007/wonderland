package io.github.pubsubseekbucket.subscribe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiService;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Snapshot;
import com.google.pubsub.v1.Subscription;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.pubsubseekbucket.util.Deserializer;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@EnableScheduling
public class Subscriber<EventType> {
    public static final int MAX_PAYLOAD_TO_LOG = 4000;

    private final String topicName;
    private final String subscriptionName;
    private final EventHandler<EventType> eventHandler;
    private final PubSubTemplate pubSubTemplate;
    private final Deserializer<PubsubMessage, EventType> deserializer;
    private final SubscriptionAdminUtil subscriptionAdminUtil;

    private com.google.cloud.pubsub.v1.Subscriber pubsubTemplateSubscriber = null;
    private Function<PubsubMessage, Integer> deliveryAttemptFunction = com.google.cloud.pubsub.v1.Subscriber::getDeliveryAttempt;

    ExecutorService executor = Executors.newFixedThreadPool(1);

    private final ApplicationEventPublisher applicationEventPublisher;

    private OptionalInt maxDeliveryAttempts = OptionalInt.empty();

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      String subscriptionName,
                      @NonNull TypeReference<EventType> eventType,
                      EventHandler<EventType> eventHandler,
                      PubSubTemplate pubSubTemplate,
                      @NonNull ObjectMapper objectMapper,
                      SubscriptionAdminUtil subscriptionAdminUtil,
                      ApplicationEventPublisher applicationEventPublisher) {
        this(topicName, subscriptionName, eventHandler, pubSubTemplate, msg -> objectMapper.readValue(msg.getData().toStringUtf8(), eventType), subscriptionAdminUtil, applicationEventPublisher);
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      String subscriptionName,
                      @NonNull TypeReference<EventType> eventType,
                      EventHandler<EventType> eventHandler,
                      PubSubTemplate pubSubTemplate,
                      @NonNull ObjectMapper objectMapper,
                      SubscriptionAdminUtil subscriptionAdminUtil) {
        this(topicName, subscriptionName, eventHandler, pubSubTemplate, msg -> objectMapper.readValue(msg.getData().toStringUtf8(), eventType), subscriptionAdminUtil, null);
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      @NonNull String subscriptionName,
                      @NonNull EventHandler<EventType> eventHandler,
                      @NonNull PubSubTemplate pubSubTemplate,
                      @NonNull Deserializer<PubsubMessage, EventType> deserializer,
                      SubscriptionAdminUtil subscriptionAdminUtil,
                      ApplicationEventPublisher applicationEventPublisher) {
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.eventHandler = eventHandler;
        this.pubSubTemplate = pubSubTemplate;
        this.deserializer = deserializer;
        this.subscriptionAdminUtil = subscriptionAdminUtil;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      Subscription subscription,
                      @NonNull TypeReference<EventType> eventType,
                      EventHandler<EventType> eventHandler,
                      PubSubTemplate pubSubTemplate,
                      @NonNull ObjectMapper objectMapper,
                      SubscriptionAdminUtil subscriptionAdminUtil,
                      ApplicationEventPublisher applicationEventPublisher) {
        this(topicName, subscription, eventHandler, pubSubTemplate, msg -> objectMapper.readValue(msg.getData().toStringUtf8(), eventType), subscriptionAdminUtil, applicationEventPublisher);
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      Subscription subscription,
                      @NonNull TypeReference<EventType> eventType,
                      EventHandler<EventType> eventHandler,
                      PubSubTemplate pubSubTemplate,
                      @NonNull ObjectMapper objectMapper,
                      SubscriptionAdminUtil subscriptionAdminUtil) {
        this(topicName, subscription, eventHandler, pubSubTemplate, msg -> objectMapper.readValue(msg.getData().toStringUtf8(), eventType), subscriptionAdminUtil, null);
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Subscriber(String topicName,
                      @NonNull Subscription subscription,
                      @NonNull EventHandler<EventType> eventHandler,
                      @NonNull PubSubTemplate pubSubTemplate,
                      @NonNull Deserializer<PubsubMessage, EventType> deserializer,
                      SubscriptionAdminUtil subscriptionAdminUtil,
                      ApplicationEventPublisher applicationEventPublisher) {
        this.topicName = topicName;
        this.subscriptionName = ProjectSubscriptionName.parse(subscription.getName()).getSubscription();
        this.eventHandler = eventHandler;
        this.pubSubTemplate = pubSubTemplate;
        this.deserializer = deserializer;
        this.subscriptionAdminUtil = subscriptionAdminUtil;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void subscribe() {
        log.info("Starting subscriber of {}", subscriptionName);
        pubsubTemplateSubscriber = pubSubTemplate.subscribe(subscriptionName, this::receiveMessage);
        pubsubTemplateSubscriber.addListener(getListener(), executor);
        maxDeliveryAttempts = subscriptionAdminUtil.getMaxDeliveryAttempts(subscriptionName);
    }

    @NotNull
    private ApiService.Listener getListener() {
        return new ApiService.Listener() {
            @Override
            public void failed(com.google.cloud.pubsub.v1.Subscriber.State from, Throwable failure) {
                log.error("Subscriber failed from state {}", from, failure);

                if (applicationEventPublisher != null) {
                    AvailabilityChangeEvent.publish(applicationEventPublisher, failure, ReadinessState.REFUSING_TRAFFIC);
                    AvailabilityChangeEvent.publish(applicationEventPublisher, failure, LivenessState.BROKEN);
                } else {
                    log.error("ApplicationEventPublisher is null, will not change the state of the application");
                }
            }
        };
    }

    @PreDestroy
    public void gracefulShutdown() {
        log.info("Graceful Shutdown started for subscriber ; {}", subscriptionName);
        unSubscribe();
        log.info("Graceful Shutdown completed for subscriber ; {}", subscriptionName);
    }

    public void unSubscribe() {
        if (pubsubTemplateSubscriber != null) {
            log.info("Stopping subscriber of {}", subscriptionName);
            pubsubTemplateSubscriber.stopAsync();
            pubsubTemplateSubscriber.awaitTerminated();

        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    protected void receiveMessage(BasicAcknowledgeablePubsubMessage ackMessage) {
        EventType event;

        try {
            event = deserializer.read(ackMessage.getPubsubMessage());
        } catch (IOException e) {
            log.error("Failed to parse message with id {} published at {}, attempt {}, error: {}, payload: {}",
                    ackMessage.getPubsubMessage().getMessageId(),
                    Instant.ofEpochSecond(ackMessage.getPubsubMessage().getPublishTime().getSeconds()),
                    deliveryAttemptFunction.apply(ackMessage.getPubsubMessage()),
                    e.getMessage(),
                    truncateIfNeeded(ackMessage.getPubsubMessage().getData().toStringUtf8(), MAX_PAYLOAD_TO_LOG)
            );
            ackMessage.nack();
            return;
        }

        try {
            eventHandler.handle(event).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.info(exception.getMessage());
                    handleException(ackMessage, event, exception);
                } else {
                    ackMessage.ack();
                }
            });
        } catch (Exception e) {
            handleException(ackMessage, event, e);
        }
    }

    private static String truncateIfNeeded(String string, int maxLength) {
        return string.length() > maxLength ? string.substring(0, maxLength) + "..." : string;
    }

    public void seekToTimestamp(Instant timestamp) {
        subscriptionAdminUtil.seekToTimestamp(subscriptionName, timestamp);
    }

    public boolean seekToSnapshot(String snapshotId) {
        return findSnapshot(snapshotId, topicName)
                .map(snapshot -> {
                    seekToSnapshot(snapshot);
                    return true;
                })
                .orElse(false);
    }

    private void seekToSnapshot(Snapshot snapshot) {
        log.debug("Seeking snapshot: {} for {}", snapshot, subscriptionName);
        subscriptionAdminUtil.seekToSnapshot(subscriptionName, snapshot.getName());
    }

    @SuppressWarnings("optional:method.invocation.invalid")
    private Optional<Snapshot> findSnapshot(String snapshotId, String topic) {
        Optional<Snapshot> maybe = subscriptionAdminUtil.listSnapshots(topic).stream()
                .filter(snapshot -> snapshot.getName().contains(snapshotId))
                .findFirst();
        var snapShot = maybe.orElse(null);
        if (snapShot != null) {
            log.debug("Found snapshot: {}", snapShot);
        } else {
            log.warn("Did not find any snapshot for topic = {} and snapshot id = {}. Found these snapshots: {}",
                    topic, snapshotId, subscriptionAdminUtil.listSnapshots(topic).stream().map(Snapshot::getName).toList());
        }
        return maybe;
    }

    private void handleException(BasicAcknowledgeablePubsubMessage ackMessage, EventType event, Throwable exception) {
        var messageId = ackMessage.getPubsubMessage().getMessageId();
        var publishTime = Instant.ofEpochSecond(ackMessage.getPubsubMessage().getPublishTime().getSeconds());
        var eventToLog = truncateIfNeeded(event.toString(), MAX_PAYLOAD_TO_LOG);

        Optional.ofNullable(deliveryAttemptFunction.apply(ackMessage.getPubsubMessage())).ifPresentOrElse(
                deliveryAttempt ->
                        maxDeliveryAttempts.ifPresentOrElse(
                                maxAttempts -> {
                                    if (deliveryAttempt < maxAttempts) {

                                        log.info("Failed to handle message with id {} published at {}, attempt {} of {}, event: {}",
                                                messageId,
                                                publishTime,
                                                deliveryAttempt,
                                                maxAttempts,
                                                eventToLog,
                                                exception);
                                    } else {
                                        log.error("Failed to handle message with id {} published at {}, attempt {} of {}, event: {}",
                                                messageId,
                                                publishTime,
                                                deliveryAttempt,
                                                maxAttempts,
                                                eventToLog,
                                                exception);
                                    }
                                },
                                () -> log.warn("Failed to handle message with id {} published at {}, attempt {}, event: {}",
                                        messageId,
                                        publishTime,
                                        deliveryAttempt,
                                        eventToLog,
                                        exception)),
                () -> log.warn("Failed to handle message with id {} published at {}, event: {}",
                        messageId,
                        publishTime,
                        eventToLog,
                        exception));
        ackMessage.nack();
    }

    void setDeliveryAttemptFunction(Function<PubsubMessage, Integer> deliveryAttemptFunction) {
        this.deliveryAttemptFunction = deliveryAttemptFunction;
    }
}
