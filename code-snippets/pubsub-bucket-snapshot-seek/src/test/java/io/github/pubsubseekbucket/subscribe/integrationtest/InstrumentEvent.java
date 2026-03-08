package io.github.pubsubseekbucket.subscribe.integrationtest;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@With
@Jacksonized
@Slf4j
public class InstrumentEvent {
    UUID instrumentId;
}
