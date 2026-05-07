package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public sealed interface AccountEvent
    extends DomainEvent
    permits AccountEvent.MoneyCredited, AccountEvent.MoneyDebited {

    @With
    @Builder
    @Jacksonized
    record MoneyDebited(UUID accountId,
                        BigDecimal amount,
                        UUID transactionId,
                        Instant occurredAt,
                        long atAccountVersion,
                        UUID id) implements AccountEvent {
        @Override
        public UUID aggregateId() {
            return accountId;
        }

        @Override
        public long atAggregateVersion() {
            return atAccountVersion;
        }
    }

    @With
    @Builder
    @Jacksonized
    record MoneyCredited(UUID accountId,
                         BigDecimal amount,
                         UUID transactionId,
                         Instant occurredAt,
                         long atAccountVersion,
                         UUID id) implements AccountEvent {
        @Override
        public UUID aggregateId() {
            return accountId;
        }

        @Override
        public long atAggregateVersion() {
            return atAccountVersion;
        }
    }
}
