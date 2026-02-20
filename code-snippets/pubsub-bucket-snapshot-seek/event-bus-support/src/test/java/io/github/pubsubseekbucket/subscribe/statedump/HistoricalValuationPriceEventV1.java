package io.github.pubsubseekbucket.subscribe.statedump;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record HistoricalValuationPriceEventV1(
        String dataPointId,
        UUID timeSeriesId,
        LocalDate date,
        long version,
        HistoricalValuationPriceV1 historicalValuationPrice,
        EventType eventType) {
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED,
        STATEDUMP
    }
}
