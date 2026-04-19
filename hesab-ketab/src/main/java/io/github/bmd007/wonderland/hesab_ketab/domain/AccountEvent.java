package io.github.bmd007.wonderland.hesab_ketab.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public sealed interface AccountEvent {

    UUID accountId();
    Instant occurredAt();

    @JsonProperty("@type")
    default String eventType() {
        return getClass().getSimpleName();
    }

    record AccountOpened(UUID accountId, String name, Instant occurredAt)
        implements AccountEvent {}

    record MoneyDeposited(UUID accountId, BigDecimal amount, Instant occurredAt)
        implements AccountEvent {}

    record MoneyDebited(UUID accountId, BigDecimal amount, UUID transactionId, Instant occurredAt)
        implements AccountEvent {}

    record MoneyCredited(UUID accountId, BigDecimal amount, UUID transactionId, Instant occurredAt)
        implements AccountEvent {}
}
