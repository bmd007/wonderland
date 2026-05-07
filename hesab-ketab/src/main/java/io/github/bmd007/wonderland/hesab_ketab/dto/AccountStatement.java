package io.github.bmd007.wonderland.hesab_ketab.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountStatement(
    String accountName,
    Instant from,
    Instant to,
    BigDecimal openingBalance,
    BigDecimal closingBalance,
    List<Entry> entries) {

    public record Entry(
        Instant occurredAt,
        String description,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal runningBalance) {
    }
}
