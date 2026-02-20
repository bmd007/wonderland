package io.github.pubsubseekbucket.publish.persisted;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Consumer;

public interface OutboxDao {
    boolean insertInOutbox(OutboxEvent outboxEvent);

    boolean insertInOutbox(Collection<OutboxEvent> events, String topicName);

    void deleteFromOutbox(OutboxEvent outboxEvent);

    void deleteFromOutbox(Collection<OutboxEvent> outboxEvents);

    void readOldInOutbox(Duration ageThreshold, Consumer<OutboxEvent> outboxEventMessageConsumer, boolean permitRandomOrder, long maxItemsToRead);

    long getOutboxSize();
}
