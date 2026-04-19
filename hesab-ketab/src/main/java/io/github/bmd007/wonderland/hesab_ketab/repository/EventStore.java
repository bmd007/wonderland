package io.github.bmd007.wonderland.hesab_ketab.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.domain.TransferRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventStore {

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    public record StoredEvent(long sequenceNumber, UUID aggregateId, String eventType) {}

    public void append(UUID aggregateId, List<AccountEvent> events, long expectedVersion) {
        long version = expectedVersion;
        for (var event : events) {
            version++;
            jdbc.sql("""
                    INSERT INTO domain_events (id, aggregate_id, event_type, payload, version)
                    VALUES (:id, :aggregateId, :eventType, :payload::jsonb, :version)
                    """)
                .param("id", UUID.randomUUID())
                .param("aggregateId", aggregateId)
                .param("eventType", event.getClass().getSimpleName())
                .param("payload", serialize(event))
                .param("version", version)
                .update();
        }
    }

    public List<AccountEvent> loadEvents(UUID aggregateId) {
        return jdbc.sql("""
                SELECT event_type, payload::text FROM domain_events
                WHERE aggregate_id = :aggregateId
                ORDER BY version
                """)
            .param("aggregateId", aggregateId)
            .query((rs, _) -> deserialize(rs.getString("event_type"), rs.getString("payload")))
            .list();
    }

    public List<StoredEvent> loadUnprocessedEvents(long afterSequence) {
        return jdbc.sql("""
                SELECT sequence_number, aggregate_id, event_type
                FROM domain_events
                WHERE sequence_number > :afterSequence
                ORDER BY sequence_number
                """)
            .param("afterSequence", afterSequence)
            .query(StoredEvent.class)
            .list();
    }

    public List<TransferRecord> findTransfersForAccount(UUID accountId) {
        return jdbc.sql("""
                SELECT
                    (debit.payload->>'transactionId')::uuid as transaction_id,
                    debit.aggregate_id as from_account_id,
                    credit.aggregate_id as to_account_id,
                    (debit.payload->>'amount')::numeric as amount,
                    a.currency,
                    debit.created_at as occurred_at
                FROM domain_events debit
                JOIN domain_events credit
                    ON debit.payload->>'transactionId' = credit.payload->>'transactionId'
                    AND credit.event_type = 'MoneyCredited'
                JOIN accounts a ON a.id = debit.aggregate_id
                WHERE debit.event_type = 'MoneyDebited'
                    AND (debit.aggregate_id = :accountId OR credit.aggregate_id = :accountId)
                ORDER BY debit.created_at DESC
                """)
            .param("accountId", accountId)
            .query(TransferRecord.class)
            .list();
    }

    private String serialize(AccountEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private AccountEvent deserialize(String eventType, String payload) {
        try {
            Class<? extends AccountEvent> clazz = switch (eventType) {
                case "AccountOpened" -> AccountEvent.AccountOpened.class;
                case "MoneyDeposited" -> AccountEvent.MoneyDeposited.class;
                case "MoneyDebited" -> AccountEvent.MoneyDebited.class;
                case "MoneyCredited" -> AccountEvent.MoneyCredited.class;
                default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
            };
            return objectMapper.readValue(payload, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
