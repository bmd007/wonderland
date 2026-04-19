package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public sealed interface AccountEvent {

    UUID accountId();
    Instant occurredAt();

    record AccountOpened(UUID accountId, String name, String currency, Instant occurredAt)
        implements AccountEvent {}

    record MoneyDeposited(UUID accountId, BigDecimal amount, Instant occurredAt)
        implements AccountEvent {}

    record MoneyDebited(UUID accountId, BigDecimal amount, UUID transactionId, Instant occurredAt)
        implements AccountEvent {}

    record MoneyCredited(UUID accountId, BigDecimal amount, UUID transactionId, Instant occurredAt)
        implements AccountEvent {}
}
