package io.github.bmd007.wonderland.hesab_ketab.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("accounts")
public record Account(
    @Id UUID id,
    String name,
    String currency,
    Instant createdAt) {
}
