package io.github.pubsubseekbucket.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import io.github.pubsubseekbucket.exception.FailedToPublishException;
import io.github.pubsubseekbucket.util.Serializer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
@Setter
public class Publisher<EventType> {
    private final String topicName;
    private final PubSubTemplate pubSubTemplate;
    private final Serializer<EventType, String> serializer;

    @SuppressFBWarnings({ "CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Publisher(@NonNull String topicName,
                     @NonNull PubSubTemplate pubSubTemplate,
                     @NonNull Serializer<EventType, String> serializer) {
        this.topicName = topicName;
        this.pubSubTemplate = pubSubTemplate;
        this.serializer = serializer;
    }

    @SuppressFBWarnings({ "CT_CONSTRUCTOR_THROW"})
    @Deprecated
    public Publisher(String topicName,
                     PubSubTemplate pubSubTemplate,
                     ObjectMapper objectMapper) {
        this(topicName, pubSubTemplate, objectMapper::writeValueAsString);
    }

    public CompletableFuture<String> publish(EventType message) {
        PubsubMessage pubsubMessage = createPubsubMessage(serializeObject(message));
        return doPublish(pubsubMessage);
    }

    public CompletableFuture<String> publish(JsonNode message) {
        PubsubMessage pubsubMessage = createPubsubMessage(message.toString());
        return doPublish(pubsubMessage);
    }

    public CompletableFuture<String> publish(PubsubMessage pubsubMessage) {
        return doPublish(pubsubMessage);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private CompletableFuture<String> doPublish(PubsubMessage pubSubMessage) {
        try {
            log.debug("Publishing message to event bus: {}", pubSubMessage);
            return pubSubTemplate.publish(topicName, pubSubMessage)
                    .whenComplete(
                            (messageId, e) -> {
                                if (e != null) {
                                    log.error("Published message was nacked: {}", pubSubMessage, e);
                                } else {
                                    log.debug("Published message was acked with id {}: {}", messageId, pubSubMessage);
                                }
                            });
        } catch (Exception e) {
            log.error("Failed to publish message {}", pubSubMessage, e);
            return CompletableFuture.failedFuture(new FailedToPublishException(pubSubMessage, e));
        }
    }

    private String serializeObject(EventType message) {
        try {
            return serializer.write(message);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write object as json string: " + message, e);
        }
    }

    private PubsubMessage createPubsubMessage(String payload) {
        return PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(payload))
                .build();
    }

    public String getTopicName() {
        return topicName;
    }
}
