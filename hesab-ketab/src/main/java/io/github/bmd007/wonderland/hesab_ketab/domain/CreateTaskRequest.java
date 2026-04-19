package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.time.Instant;

public record CreateTaskRequest(String taskType, String payload, Instant scheduledAt) {
}
