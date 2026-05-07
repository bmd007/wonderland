package io.github.bmd007.wonderland.hesab_ketab.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Comparable<DomainEvent> {

    UUID id();

    UUID aggregateId();

    Instant occurredAt();

    long atAggregateVersion();

    @JsonProperty("@type")//todo what is the @?
    default String type() {
        return getClass().getSimpleName();
    }

    @Override
    default int compareTo(DomainEvent o) {
        return occurredAt().compareTo(o.occurredAt());
    }
}
