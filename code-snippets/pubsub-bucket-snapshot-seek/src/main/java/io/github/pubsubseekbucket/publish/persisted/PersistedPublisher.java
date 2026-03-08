package io.github.pubsubseekbucket.publish.persisted;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import io.github.pubsubseekbucket.publish.Publisher;
import io.github.pubsubseekbucket.util.EventBusScheduler;
import io.github.pubsubseekbucket.util.Serializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * This event publisher offers guaranteed at-least-once delivery once the transaction scope of the {@link #persistEvent(Object)} has been committed
 * (if present, may be omitted if not required).
 * <p>
 * Use the {@link io.github.pubsubseekbucket.publish.PublisherFactory} class to create PersistedPublisher instances.
 * <p>
 * The caller is responsible for ensuring the correct sequence of related state changes prior to committing the related transaction scope.
 * See {@link org.springframework.transaction.annotation.Propagation#SUPPORTS} usage in {@link PostgresOutboxDao}.
 * <p>
 * The caller is responsible for keeping track of the {@link PublishTrigger} instance returned from persistEvent()/persistEvents() calls.
 * Once the transaction scope is completed, the caller should invoke the {@link PublishTrigger#publish()} method to ensure a timely delivery.
 * <p>
 * This implementation will persist the full outgoing PubSub messages, to ensure that any provided header is retained on re-transmission.
 * <p>
 * Note: If the destination of this publisher is a PubSub topic with schema validation enabled, there is no means to drop
 * non-conformal messages from the Outbox in this implementation. Consider using/adding a custom Drainer in that case with
 * DLQ handling of stuck messages.
 *
 * @param <T> Event type of this publisher, corresponding to the underlying PubSub topic spec.
 */
@Slf4j
public class PersistedPublisher<T> {

    private final String topicName;
    private final Publisher<T> publisher;
    private final Function<T, String> idExtractor;
    private final Function<T, Long> versionExtractor;
    private final Serializer<T, String> payloadSerializer;
    private final OutboxDao outboxDao;
    private final PublishAckHandler publishAckHandler;

    private final JsonFormat.Printer jsonPrinter;
    private final JsonFormat.Parser jsonParser;

    @Deprecated
    public PersistedPublisher(
            String topicName,
            Publisher<T> publisher,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer,
            OutboxDao outboxDao) {
        this(topicName, publisher, idExtractor, versionExtractor, serializer, outboxDao, 1, Duration.ZERO, null);
    }

    @Deprecated
    public PersistedPublisher(
            String topicName,
            Publisher<T> publisher,
            Function<T, String> idExtractor,
            Function<T, Long> versionExtractor,
            Serializer<T, String> serializer,
            OutboxDao outboxDao,
            int maxAckBatchSize,
            Duration maxAckDelay,
            EventBusScheduler eventBusScheduler) {
        this.topicName = topicName;
        this.publisher = publisher;
        this.idExtractor = idExtractor;
        this.versionExtractor = versionExtractor;
        this.payloadSerializer = serializer;
        this.outboxDao = outboxDao;

        this.publishAckHandler = new PublishAckHandler(outboxDao, maxAckBatchSize, maxAckDelay, eventBusScheduler);
        this.jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();
        this.jsonParser = JsonFormat.parser();
    }

    /**
     * Write a single outgoing event to the persisted outbox
     *
     * @param event The PubSub message payload
     * @return a PublishTrigger to invoke once the relevant transaction scope has been committed
     * @throws FailedToPersistException if the event cannot be added to the outbox
     */
    @Deprecated
    public PublishTrigger persistEvent(T event) throws FailedToPersistException {
        return doPersistEvent(event, PubsubMessage.newBuilder());
    }

    /**
     * Write a collection of outgoing events to the persisted outbox as a batch
     *
     * @param events the payloads to send as individual PubSub messages
     * @return a PublishTrigger to invoke once the relevant transaction scope has been committed
     * @throws FailedToPersistException if the event cannot be added to the outbox
     */
    @Deprecated
    public PublishTrigger persistEvents(Collection<T> events) throws FailedToPersistException {
        return doPersistEvents(events, e -> PubsubMessage.newBuilder());
    }

    /**
     * Write a single outgoing event to the persisted outbox together with a PubSub message builder for any custom headers that should be included.
     *
     * @param event   the message payload to include in the PubSub message
     * @param builder a PubSub message builder for custom headers
     * @return a PublishTrigger to invoke once the relevant transaction scope has been committed
     * @throws FailedToPersistException if the event cannot be added to the outbox
     */
    @Deprecated
    public PublishTrigger persistEvent(T event, PubsubMessage.Builder builder) throws FailedToPersistException {
        return doPersistEvent(event, builder);
    }

    /**
     * Write a collection of events to the persisted outbox as a batch, using the provided PubSub message builder function to construct the actual messages.
     *
     * @param events          the payloads to send as individual PubSub messages
     * @param builderFunction a function to create a custom PubSub message builder for each outgoing event
     * @return a PublishTrigger to invoke once the relevant transaction scope has been committed
     * @throws FailedToPersistException if the events cannot be added to the outbox
     */
    @Deprecated
    public PublishTrigger persistEvents(Collection<T> events, Function<T, PubsubMessage.Builder> builderFunction) throws FailedToPersistException {
        return doPersistEvents(events, builderFunction);
    }

    public String getTopicName() {
        return topicName;
    }

    private PublishTrigger doPersistEvent(T event, PubsubMessage.Builder builder) throws FailedToPersistException {

        final PubsubMessage message = buildPubsubMessage(builder, event);
        final OutboxEvent outboxEvent = createOutboxEvent(event, message);

        if (!outboxDao.insertInOutbox(outboxEvent)) {
            throw new FailedToPersistException("Could not persist event in outbox");
        }
        return () -> attemptPublish(message, outboxEvent);
    }

    private PublishTrigger doPersistEvents(Collection<T> events, Function<T, PubsubMessage.Builder> builderFunction) throws FailedToPersistException {
        final List<Pair<PubsubMessage, OutboxEvent>> outgoingObjects = events.stream().map(event -> {
            var pubsubMessage = buildPubsubMessage(builderFunction.apply(event), event);
            var outboxEvent = createOutboxEvent(event, pubsubMessage);
            return Pair.of(pubsubMessage, outboxEvent);
        }).toList();

        final List<OutboxEvent> outboxEvents = outgoingObjects.stream()
                .map(Pair::getRight)
                .toList();

        if (!outboxDao.insertInOutbox(outboxEvents, topicName)) {
            throw new FailedToPersistException("Could not persist events in outbox");
        }

        return () -> outgoingObjects.forEach(pair -> attemptPublish(pair.getLeft(), pair.getRight()));
    }

    private void attemptPublish(PubsubMessage message, OutboxEvent outboxEvent) {
        publisher.publish(message).whenComplete((s, e) -> {
            if (e == null) {
                publishAckHandler.handle(outboxEvent);
            } else {
                log.warn("Failed to publish {}", outboxEvent, e);
            }
        });
    }

    private OutboxEvent createOutboxEvent(T event, PubsubMessage message) {
        final String pubsubMessageAsStr;
        try {
            pubsubMessageAsStr = jsonPrinter.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new FailedToPersistException("Could not serialize PubsubMessage as json", e);
        }
        return new OutboxEvent(idExtractor.apply(event), versionExtractor.apply(event), topicName, pubsubMessageAsStr);
    }

    private PubsubMessage buildPubsubMessage(PubsubMessage.Builder builder, T event) {
        final String payloadAsStr;
        try {
            payloadAsStr = payloadSerializer.write(event);
        } catch (IOException e) {
            throw new FailedToPersistException("Could not serialize event", e);
        }

        builder.setData(ByteString.copyFromUtf8(payloadAsStr));
        return builder.build();
    }

    void rePublish(OutboxEvent outboxEvent) {
        final PubsubMessage.Builder msg = PubsubMessage.newBuilder();
        try {
            jsonParser.merge(outboxEvent.pubsubMessage(), msg);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse outbox message", e);
            return;
        }

        msg.putAttributes("outbox_resend", "true");

        publisher.publish(msg.build()).whenComplete((s, e) -> {
            if (e == null) {
                publishAckHandler.handle(outboxEvent);
            } else {
                log.warn("Failed to re-publish msg {}", msg, e);
            }
        });
    }

}
