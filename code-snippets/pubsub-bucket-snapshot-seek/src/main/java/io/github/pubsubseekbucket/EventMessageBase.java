package io.github.pubsubseekbucket;

import java.util.UUID;

public interface EventMessageBase {
    UUID getId();

    int getVersion();
}
