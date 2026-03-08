package io.github.pubsubseekbucket.subscribe;

import java.util.concurrent.CompletableFuture;

public interface EventHandler<EventType> {
    CompletableFuture<?> handle(EventType event);
}
