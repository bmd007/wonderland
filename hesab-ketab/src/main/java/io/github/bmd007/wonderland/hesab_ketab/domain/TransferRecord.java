package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record TransferRecord(
    UUID transactionId,
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    Instant occurredAt) {
}
