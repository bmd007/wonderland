package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.math.BigDecimal;
import java.util.UUID;

public sealed class LedgerException extends RuntimeException {

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

    public static final class IncompatibleCurrencies extends LedgerException {
        private final String from;
        private final String to;
        public IncompatibleCurrencies(String from, String to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public String getMessage() {
            return "Cannot transfer between different currencies: %s and %s".formatted(from, to);
        }
    }
}
