package io.github.bmd007.wonderland.hesab_ketab.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("scheduled_tasks")
public record ScheduledTask(
    @Id UUID id,
    String taskType,
    String payload,
    TaskStatus status,
    Instant scheduledAt,
    Instant lockedAt,
    Instant completedAt,
    String errorMessage) {
}
