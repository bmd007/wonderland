package io.github.bmd007.wonderland.hesab_ketab.listener;

import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import io.github.bmd007.wonderland.hesab_ketab.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private static final String CHANNEL = "domain_event";
    private static final String CONSUMER_NAME = "main-consumer";

    private final DataSource dataSource;
    private final JdbcClient jdbc;
    private final EventStore eventStore;
    private final TaskService taskService;
    private Connection listenConnection;
    private boolean initialCatchUpDone = false;

    @Scheduled(fixedDelay = 200)
    public void poll() {
        try {
            boolean hasNotifications = drainNotifications();
            if (hasNotifications || !initialCatchUpDone) {
                catchUp();
                initialCatchUpDone = true;
            }
        } catch (Exception e) {
            log.error("Error in event consumer", e);
            closeListenConnection();
            initialCatchUpDone = false;
        }
    }

    private boolean drainNotifications() throws SQLException {
        var conn = getListenConnection();
        try (var stmt = conn.createStatement()) {
            stmt.execute("");
        }
        var pgConn = conn.unwrap(PGConnection.class);
        var notifications = pgConn.getNotifications();
        return notifications != null && notifications.length > 0;
    }

    private void catchUp() {
        long lastSequence = getLastProcessedSequence();
        var events = eventStore.loadUnprocessedEvents(lastSequence);
        for (var event : events) {
            processEvent(event);
            updateOffset(event.sequenceNumber());
        }
        if (!events.isEmpty()) {
            log.debug("Processed {} events, last sequence: {}", events.size(), events.getLast().sequenceNumber());
        }
    }

    private void processEvent(EventStore.StoredEvent event) {
        switch (event.eventType()) {
            case "MoneyDebited", "MoneyCredited" ->
                taskService.scheduleBalanceCheck(event.aggregateId());
            default -> log.debug("Event: {} seq={}", event.eventType(), event.sequenceNumber());
        }
    }

    private long getLastProcessedSequence() {
        return jdbc.sql("SELECT last_sequence FROM event_consumer_offsets WHERE consumer_name = :name")
            .param("name", CONSUMER_NAME)
            .query(Long.class)
            .optional()
            .orElse(0L);
    }

    private void updateOffset(long sequence) {
        jdbc.sql("""
                INSERT INTO event_consumer_offsets (consumer_name, last_sequence, updated_at)
                VALUES (:name, :seq, now())
                ON CONFLICT (consumer_name) DO UPDATE SET last_sequence = :seq, updated_at = now()
                """)
            .param("name", CONSUMER_NAME)
            .param("seq", sequence)
            .update();
    }

    private Connection getListenConnection() throws SQLException {
        if (listenConnection == null || listenConnection.isClosed()) {
            listenConnection = dataSource.getConnection();
            listenConnection.setAutoCommit(true);
            try (var stmt = listenConnection.createStatement()) {
                stmt.execute("LISTEN " + CHANNEL);
            }
            log.info("Listening on channel '{}'", CHANNEL);
        }
        return listenConnection;
    }

    private void closeListenConnection() {
        try {
            if (listenConnection != null && !listenConnection.isClosed()) {
                listenConnection.close();
            }
        } catch (SQLException e) {
            log.warn("Error closing listen connection", e);
        }
        listenConnection = null;
    }
}
