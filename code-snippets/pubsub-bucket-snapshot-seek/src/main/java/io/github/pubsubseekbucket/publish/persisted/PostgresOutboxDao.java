package io.github.pubsubseekbucket.publish.persisted;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// CPD-OFF
@Slf4j
public class PostgresOutboxDao implements OutboxDao {
    private static final String SELECT_OLD_EVENTS_FROM_OUTBOX_RANDOM_ORDER = """
            select id, version, topic_name, data from outbox
                where created < (current_timestamp - make_interval(secs := :minAgeSeconds))
                order by random()
                limit :limit""";

    private static final String SELECT_OLD_EVENTS_FROM_OUTBOX = """
            select id, version, topic_name, data from outbox
                where created < (current_timestamp - make_interval(secs := :minAgeSeconds))
                limit :limit""";

    private static final String INSERT_IN_OUTBOX = """
            insert into outbox(data, created, id, version, topic_name)
                values(:data, current_timestamp, :id, :version, :topicName)""";

    private static final String BATCH_INSERT_IN_OUTBOX = """
            insert into outbox(data, created, id, version, topic_name)
                select v0.data, current_timestamp, v0.id, v0.version, :topicName
                    from  (
                        select * from unnest(:dataValues, :idValues, :versionValues)
                    ) as v0(data, id, version)""";

    private static final String DELETE_FROM_OUTBOX = """
            delete from outbox
                where id=:id and version=:version and topic_name=:topicName""";

    private static final String COUNT_EVENTS_OUTBOX = """
            select count(*) from outbox""";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostgresOutboxDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean insertInOutbox(OutboxEvent outboxEvent) {
        try {
            jdbcTemplate.update(INSERT_IN_OUTBOX,
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
    public boolean insertInOutbox(Collection<OutboxEvent> events, String topicName) {

        try {
            var dataValues = events.stream().map(OutboxEvent::pubsubMessage).toArray(String[]::new);
            var idValues = events.stream().map(OutboxEvent::id).toArray(String[]::new);
            var versionValues = events.stream().map(OutboxEvent::version).toArray(Long[]::new);

            jdbcTemplate.update(BATCH_INSERT_IN_OUTBOX,
                    Map.of(
                            "dataValues", dataValues,
                            "idValues", idValues,
                            "versionValues", versionValues,
                            "topicName", topicName)
            );
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
    public void deleteFromOutbox(OutboxEvent outboxEvent) {
        var update = jdbcTemplate.update(
                DELETE_FROM_OUTBOX,
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
    public void deleteFromOutbox(Collection<OutboxEvent> events) {
        var idValues = events.stream().map(OutboxEvent::id).toArray(String[]::new);
        var versionValues = events.stream().map(OutboxEvent::version).toArray(Long[]::new);
        var topicValues = events.stream().map(OutboxEvent::topicName).toArray(String[]::new);

        var update = jdbcTemplate.update(
                """
                          delete
                          from outbox o
                          using (select * from unnest(:idValues, :versionValues, :topicValues)) as v0(id, version, topic_name)
                                        where o.id = v0.id
                                          and o.version = v0.version
                                          and o.topic_name = v0.topic_name;
                        """,
                new MapSqlParameterSource()
                        .addValue("idValues", idValues)
                        .addValue("versionValues", versionValues)
                        .addValue("topicValues", topicValues)
        );

        if (update < events.size()) {
            log.warn("Not all events to delete from outbox exist, {} events deleted out of {}.", update, events.size());
        } else if (update > events.size()) {
            log.error("Deleted {} rows (expected {}}) from outbox.", update, events.size());
        } else {
            log.debug("Deleted {} events from outbox.", update);
        }
    }

    @Override
    @Transactional
    public void readOldInOutbox(Duration ageThreshold, Consumer<OutboxEvent> outboxEventMessageConsumer, boolean permitRandomOrder, long maxItemsToRead) {
        try {
            double minAgeSeconds = (double) ageThreshold.toMillis() / TimeUnit.SECONDS.toMillis(1);
            jdbcTemplate.getJdbcTemplate().setFetchSize(1_000);
            jdbcTemplate.query(
                    permitRandomOrder ? SELECT_OLD_EVENTS_FROM_OUTBOX_RANDOM_ORDER : SELECT_OLD_EVENTS_FROM_OUTBOX,
                    Map.of("minAgeSeconds", minAgeSeconds,
                            "limit", maxItemsToRead
                    ),
                    createOutboxEventMessageHandler(outboxEventMessageConsumer)
            );
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
                    COUNT_EVENTS_OUTBOX,
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
