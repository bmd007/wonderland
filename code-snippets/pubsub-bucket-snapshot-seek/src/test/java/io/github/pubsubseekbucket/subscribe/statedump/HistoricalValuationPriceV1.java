package io.github.pubsubseekbucket.subscribe.statedump;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record HistoricalValuationPriceV1(
        String dataPointId,
        UUID custodyInstrumentId,
        String currency,
        BigDecimal price,
        LocalDate date,
        BigDecimal priceFactor,
        long version,
        String priceSource,
        String priceType
        ) {
}
