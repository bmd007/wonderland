package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description) {
}
