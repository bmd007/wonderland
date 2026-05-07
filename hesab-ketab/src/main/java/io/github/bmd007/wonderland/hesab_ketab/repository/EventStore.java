package io.github.bmd007.wonderland.hesab_ketab.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventStore {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public void append(DomainEvent... events) {
        var sorted = Arrays.stream(events)
            .sorted()
            .toList();
        jdbcClient.sql("""
                INSERT INTO domain_events (id, aggregate_id, event_type, aggregate_version, payload)
                SELECT * FROM unnest(
                    :ids::uuid[],
                    :aggregateIds::uuid[],
                    :eventTypes::text[],
                    :aggregateVersions::int[],
                    :payloads::jsonb
                )
                """)
            .param("ids", sorted.stream().map(DomainEvent::id).toArray(UUID[]::new))
            .param("aggregateIds", sorted.stream().map(DomainEvent::aggregateId).toArray(UUID[]::new))
            .param("aggregateVersions", sorted.stream().mapToLong(DomainEvent::atAggregateVersion).toArray())
            .param("eventTypes", sorted.stream().map(DomainEvent::type).toArray(String[]::new))
            .param("payloads", sorted.stream().map(this::serialize).toArray(String[]::new))
            .update();
    }

    public List<DomainEvent> loadEventsUpTo(UUID aggregateId, Instant asOf) {
        return jdbcClient.sql("""
                SELECT event_type, payload::text FROM domain_events
                WHERE aggregate_id = :aggregateId AND created_at <= :asOf
                ORDER BY aggregate_version
                """)
            .param("aggregateId", aggregateId)
            .param("asOf", Timestamp.from(asOf))
            .query((rs, _) -> deserialize(rs.getString("event_type"), rs.getString("payload")))
            .list();
    }

    public List<DomainEvent> loadEventsBetween(UUID aggregateId, Instant from, Instant to) {
        return jdbcClient.sql("""
                SELECT event_type, payload::text FROM domain_events
                WHERE aggregate_id = :aggregateId AND created_at > :from AND created_at <= :to
                ORDER BY aggregate_version
                """)
            .param("aggregateId", aggregateId)
            .param("from", Timestamp.from(from))
            .param("to", Timestamp.from(to))
            .query((rs, _) -> deserialize(rs.getString("event_type"), rs.getString("payload")))
            .list();
    }

    public Optional<FetchedDomainEvent> loadNextUnprocessedEvent(String consumerName) {
        return jdbcClient.sql("""
                SELECT event_type, payload::text, sequence_number
                FROM domain_events
                WHERE sequence_number > COALESCE((SELECT last_sequence FROM event_consumer_offsets WHERE consumer_name = :name), 0)
                ORDER BY sequence_number
                LIMIT 1
                """)
            .param("name", consumerName)
            .query((rs, _) -> new FetchedDomainEvent(
                deserialize(rs.getString("event_type"), rs.getString("payload")),
                rs.getLong("sequence_number")
            ))
            .optional();
    }

    public List<DomainEvent> loadEvents(UUID aggregateId) {
        return jdbcClient.sql("""
                SELECT event_type, payload::text FROM domain_events
                WHERE aggregate_id = :aggregateId
                ORDER BY version
                """)
            .param("aggregateId", aggregateId)
            .query((rs, _) -> deserialize(rs.getString("event_type"), rs.getString("payload")))
            .list();
    }

    @SneakyThrows
    private String serialize(DomainEvent event) {
        return objectMapper.writeValueAsString(event);
    }

    private DomainEvent deserialize(String eventType, String payload) {
        try {
            Class<? extends AccountEvent> clazz = switch (eventType) {
                case "MoneyDebited" -> AccountEvent.MoneyDebited.class;
                case "MoneyCredited" -> AccountEvent.MoneyCredited.class;
                default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
            };
            return objectMapper.readValue(payload, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record FetchedDomainEvent(DomainEvent domainEvent, long sequenceNumber) {
    }
}
