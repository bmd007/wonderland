package io.github.bmd007.wonderland.hesab_ketab.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountBalance(
    UUID id,
    String name,
    String currency,
    Instant createdAt,
    BigDecimal balance) {
}
