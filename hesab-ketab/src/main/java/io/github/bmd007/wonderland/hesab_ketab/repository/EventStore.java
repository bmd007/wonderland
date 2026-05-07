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
        Arrays.stream(events)
            .sorted()
            .forEach(event -> jdbcClient.sql("""
                    INSERT INTO domain_events (id, aggregate_id, event_type, at_aggregate_version, payload)
                    VALUES (:id, :aggregateId, :eventType, :atAggregateVersion, :payload::jsonb)
                    """)
                .param("id", event.id())
                .param("aggregateId", event.aggregateId())
                .param("eventType", event.type())
                .param("atAggregateVersion", event.atAggregateVersion())
                .param("payload", serialize(event))
                .update());
    }

    public List<DomainEvent> loadEventsUpTo(UUID aggregateId, Instant asOf) {
        return jdbcClient.sql("""
                SELECT event_type, payload::text FROM domain_events
                WHERE aggregate_id = :aggregateId AND created_at <= :asOf
                ORDER BY at_aggregate_version
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
                ORDER BY at_aggregate_version
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
                ORDER BY at_aggregate_version
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
