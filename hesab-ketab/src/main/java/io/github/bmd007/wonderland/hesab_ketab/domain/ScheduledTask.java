package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@Builder
@With
public record ScheduledTask(
    UUID id,
    String taskType,
    String payload,
    TaskStatus status,
    Instant scheduledAt,
    Instant lockedAt,
    Instant completedAt,
    String errorMessage) {
}
