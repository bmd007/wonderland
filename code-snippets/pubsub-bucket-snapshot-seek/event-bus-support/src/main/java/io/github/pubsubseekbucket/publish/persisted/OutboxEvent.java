package io.github.pubsubseekbucket.publish.persisted;

public record OutboxEvent(String id, long version, String topicName, String pubsubMessage) {
}
