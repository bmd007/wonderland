package io.github.bmd007.wonderland.hesab_ketab.listener;

import io.github.bmd007.wonderland.hesab_ketab.domain.AccountAggregate;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private static final String CHANNEL = "domain_event";

    private final DataSource dataSource;
    private final JdbcClient jdbc;
    private final EventStore eventStore;
    private final AccountRepository accountRepository;
    private final TransactionTemplate transactionTemplate;

    private final String consumerName = "consumer-" + ManagementFactory.getRuntimeMXBean().getName();
    private ExecutorService executor;
    private Connection listenConnection;
    private volatile boolean running = true;
    private boolean initialCatchUpDone = false;

    @PostConstruct
    void start() {
        log.info("Starting event consumer '{}'", consumerName);
        executor = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("event-consumer").factory());
        executor.submit(this::pollLoop);
    }

    @PreDestroy
    void shutdown() {
        running = false;
        executor.shutdown();
        closeListenConnection();
    }

    private void pollLoop() {
        while (running) {
            try {
                boolean hasNotifications = drainNotifications();
                if (hasNotifications || !initialCatchUpDone) {
                    catchUp();
                    initialCatchUpDone = true;
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in event consumer", e);
                closeListenConnection();
                initialCatchUpDone = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("Event consumer stopped");
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
            transactionTemplate.executeWithoutResult(_ -> {
                updateProjection(event);
                updateOffset(event.sequenceNumber());
            });
        }
        if (!events.isEmpty()) {
            log.debug("Projected {} events, last sequence: {}", events.size(), events.getLast().sequenceNumber());
        }
    }

    private void updateProjection(EventStore.StoredEvent event) {
        var events = eventStore.loadEvents(event.aggregateId());
        if (events.isEmpty()) {
            log.warn("No events found for aggregate {}", event.aggregateId());
            return;
        }
        var aggregate = AccountAggregate.reconstitute(events);
        accountRepository.save(aggregate.toSnapshot());
        log.debug("Projection updated for {}: balance={}, version={}",
            event.aggregateId(), aggregate.balance(), aggregate.version());
    }

    private long getLastProcessedSequence() {
        return jdbc.sql("SELECT last_sequence FROM event_consumer_offsets WHERE consumer_name = :name")
            .param("name", consumerName)
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
            .param("name", consumerName)
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
