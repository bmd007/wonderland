package io.github.pubsubseekbucket.publish.persisted;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.MILLIS;

// CPD-OFF

@Slf4j
public class OracleOutboxDao implements OutboxDao {
    private static final String SELECT_OLD_EVENTS_FROM_OUTBOX_RANDOM_ORDER = """
            select * from (
                select id, version, topic_name, data, created from %s.outbox
                    where created < :createdThreshold
                    order by dbms_random.value
                )
                where rownum <= :limit""";

    private static final String SELECT_OLD_EVENTS_FROM_OUTBOX = """
            select * from (
                select id, version, topic_name, data, created from %s.outbox
                    where created < :createdThreshold
                )
                where rownum <= :limit""";

    private static final String INSERT_IN_OUTBOX = """
            insert into %s.outbox(data, created, id, version, topic_name)
                values(:data, systimestamp, :id, :version, :topicName)""";

    private static final String DELETE_FROM_OUTBOX = """
            delete from %s.outbox
                where id=:id and version=:version and topic_name=:topicName""";

    private static final String COUNT_EVENTS_OUTBOX = """
            select count(*) from %s.outbox""";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String outboxSchema;

    public OracleOutboxDao(NamedParameterJdbcTemplate jdbcTemplate, String outboxSchema) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxSchema = outboxSchema;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    public boolean insertInOutbox(OutboxEvent outboxEvent) {
        try {
            jdbcTemplate.update(INSERT_IN_OUTBOX.formatted(outboxSchema),
                    new MapSqlParameterSource()
                            .addValue("id", outboxEvent.id())
                            .addValue("version", outboxEvent.version())
                            .addValue("topicName", outboxEvent.topicName())
                            .addValue("data", outboxEvent.pubsubMessage())
            );
            log.debug("Inserted in event outbox: {}", outboxEvent);
        } catch (DuplicateKeyException e) {
            log.debug("Failed to insert {} into outbox because of duplicate key.", outboxEvent, e);
            return false;
        } catch (DataAccessException dataAccessException) {
            log.error("Error inserting event into outbox: " + outboxEvent, dataAccessException);
            throw dataAccessException;
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    public boolean insertInOutbox(Collection<OutboxEvent> events, String topicName) {

        try {
            var result = jdbcTemplate.batchUpdate(
                    INSERT_IN_OUTBOX.formatted(outboxSchema),
                    SqlParameterSourceUtils.createBatch(events.stream().map(outboxEvent -> {
                                var row = new HashMap<>();
                                row.put("id", outboxEvent.id());
                                row.put("version", outboxEvent.version());
                                row.put("topicName", topicName);
                                row.put("data", outboxEvent.pubsubMessage());
                                return row;
                            }
                    ).toArray()));

            if (result.length != events.size() || Arrays.stream(result).anyMatch(i -> i != 1)) {
                throw new IllegalStateException("Batch insert of %d events resulted in %s updates".formatted(events.size(), Arrays.toString(result)));
            }

            log.debug("Inserted {} events in outbox for topic {}", events.size(), topicName);
        } catch (DuplicateKeyException e) {
            log.debug("Failed to insert {} events into outbox because of duplicate key.", events.size());
            return false;
        } catch (DataAccessException dataAccessException) {
            log.error("Error inserting {} events into outbox: ", events.size(), dataAccessException);
            throw dataAccessException;
        }

        return true;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    public void deleteFromOutbox(OutboxEvent outboxEvent) {
        var update = jdbcTemplate.update(
                DELETE_FROM_OUTBOX.formatted(outboxSchema),
                new MapSqlParameterSource()
                        .addValue("id", outboxEvent.id())
                        .addValue("version", outboxEvent.version())
                        .addValue("topicName", outboxEvent.topicName())
        );

        if (update == 0) {
            log.warn("Event to delete from outbox does not exist, event {} with version {} and topic name {}.", outboxEvent.id(), outboxEvent.version(), outboxEvent.topicName());
        } else if (update > 1) {
            log.error("Deleted {} rows (expected 1) from outbox when trying to delete event {} with version {} and topic name {}.", update, outboxEvent.id(), outboxEvent.version(), outboxEvent.topicName());
        } else {
            log.debug("Deleted event from outbox with id {} and version {} and topic name {}.", outboxEvent.id(), outboxEvent.version(), outboxEvent.topicName());
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    public void deleteFromOutbox(Collection<OutboxEvent> events) {
        var result = jdbcTemplate.batchUpdate(
                DELETE_FROM_OUTBOX.formatted(outboxSchema),
                SqlParameterSourceUtils.createBatch(events.stream().map(outboxEvent -> {
                            var row = new HashMap<>();
                            row.put("id", outboxEvent.id());
                            row.put("version", outboxEvent.version());
                            row.put("topicName", outboxEvent.topicName());
                            return row;
                        }
                ).toArray()));

        if (result.length != events.size() || Arrays.stream(result).anyMatch(i -> i != 1)) {
            log.warn("Batch delete of %d events resulted in %s updates".formatted(events.size(), Arrays.toString(result)));
        }

        log.debug("Deleted {} events from outbox", events.size());
    }

    @Override
    @Transactional
    public void readOldInOutbox(Duration ageThreshold, Consumer<OutboxEvent> outboxEventMessageConsumer, boolean permitRandomOrder, long maxItemsToRead) {
        try {
            jdbcTemplate.getJdbcTemplate().setFetchSize(1_000);
            jdbcTemplate.query(
                    (permitRandomOrder ? SELECT_OLD_EVENTS_FROM_OUTBOX_RANDOM_ORDER : SELECT_OLD_EVENTS_FROM_OUTBOX).formatted(outboxSchema),
                    Map.of("createdThreshold", Timestamp.from(Instant.now().minus(ageThreshold.toMillis(), MILLIS)),
                            "limit", maxItemsToRead
                    ),
                    createOutboxEventMessageHandler(outboxEventMessageConsumer));
        } catch (DataAccessException dataAccessException) {
            log.error("Error getting old events from outbox", dataAccessException);
            throw dataAccessException;
        } finally {
            // Reset after batch
            jdbcTemplate.getJdbcTemplate().setFetchSize(-1);
        }
    }

    private RowCallbackHandler createOutboxEventMessageHandler(Consumer<OutboxEvent> outboxEventMessageConsumer) {
        return rs -> {
            OutboxEvent outboxEvent = new OutboxEvent(rs.getString("id"), rs.getLong("version"), rs.getString("topic_name"), rs.getString("data"));
            outboxEventMessageConsumer.accept(outboxEvent);
        };
    }

    @Override
    public long getOutboxSize() {
        try {
            final Long result = jdbcTemplate.queryForObject(
                    COUNT_EVENTS_OUTBOX.formatted(outboxSchema),
                    Collections.emptyMap(),
                    Long.class
            );
            return result == null ? 0 : result;
        } catch (DataAccessException e) {
            log.error("Error counting size of event outbox", e);
            return 0;
        }
    }
}
