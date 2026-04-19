package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@With
public record Account(UUID id, String name, BigDecimal balance, long version, Instant createdAt) {
}
