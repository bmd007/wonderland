package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

public sealed class LedgerException extends RuntimeException {

    @Getter
    public static final class AccountNotFound extends LedgerException {
        private final UUID accountId;

        public AccountNotFound(UUID accountId) {
            this.accountId = accountId;
        }

        @Override
        public String getMessage() {
            return "Account not found: " + accountId;
        }
    }

    @Getter
    public static final class ConcurrencyConflict extends LedgerException {
        private final UUID aggregateId;
        public ConcurrencyConflict(UUID aggregateId) {
            this.aggregateId = aggregateId;
        }
        @Override
        public String getMessage() {
            return "Concurrent modification detected for account: " + aggregateId;
        }
    }

    @Getter
    public static final class InsufficientBalance extends LedgerException {
        private final UUID accountId;
        private final BigDecimal requested;
        private final BigDecimal available;

        public InsufficientBalance(UUID accountId, BigDecimal requested, BigDecimal available) {
            this.accountId = accountId;
            this.requested = requested;
            this.available = available;
        }

        @Override
        public String getMessage() {
            return "Insufficient balance in %s: requested %s, available %s".formatted(accountId, requested, available);
        }
    }
}
