package io.github.bmd007.wonderland.hesab_ketab.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("transactions")
public record Transaction(
    @Id UUID id,
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    String currency,
    String description,
    Instant createdAt) {
}
