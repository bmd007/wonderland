package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountStatement(
    UUID accountId,
    String accountName,
    List<StatementEntry> entries,
    BigDecimal openingBalance,
    BigDecimal closingBalance) {

    public record StatementEntry(
        Instant occurredAt,
        String description,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal runningBalance) {
    }
}
