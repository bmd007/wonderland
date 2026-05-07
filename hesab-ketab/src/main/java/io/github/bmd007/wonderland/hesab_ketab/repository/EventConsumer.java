package io.github.bmd007.wonderland.hesab_ketab.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class EventConsumer {

    private static final String CHANNEL = "domain_event";
    private static final int NOTIFICATION_TIMEOUT_MS = 500;

    private final JdbcClient jdbc;
    private final DataSource dataSource;
    private final EventStore eventStore;
    private final AccountRepository accountRepository;
    private final TransactionTemplate transactionTemplate;
    private final String consumerName;
    private Connection rawConnection;
    private PGConnection pgConnection;
    private volatile boolean running = true;

    public EventConsumer(
        JdbcClient jdbc,
        DataSource dataSource,
        EventStore eventStore,
        AccountRepository accountRepository,
        TransactionTemplate transactionTemplate,
        @Value("${POD_ID:pod1}") String podId
    ) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.eventStore = eventStore;
        this.accountRepository = accountRepository;
        this.transactionTemplate = transactionTemplate;
        this.consumerName = "account-projector-" + podId;
    }

    @PostConstruct
    void start() {
        log.info("Starting event consumer '{}'", consumerName);
        Thread.ofVirtual()
            .name("event-consumer")
            .start(this::listenLoop);
    }

    @PreDestroy
    void shutdown() {
        running = false;
        closeConnection();
    }

    private void listenLoop() {
        catchUp();
        while (running) {
            try {
                if (awaitNotification()) {
                    catchUp();
                }
            } catch (Exception e) {
                log.error("Error in event consumer", e);
                closeConnection();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("Event consumer '{}' stopped", consumerName);
    }

    private boolean awaitNotification() throws SQLException {
        ensureConnection();
        var notifications = pgConnection.getNotifications(NOTIFICATION_TIMEOUT_MS);
        return notifications != null && notifications.length > 0;
    }

    private void catchUp() {
        while (running) {
            var processed = transactionTemplate.execute(_ -> {
                var fetched = eventStore.loadNextUnprocessedEvent(consumerName);
                fetched.ifPresent(event -> {
                    var aggregateId = event.domainEvent().aggregateId();
                    var account = accountRepository.findById(aggregateId).orElseThrow();
                    var result = account.apply(event.domainEvent());
                    if (result.succeed()) {
                        accountRepository.save(result.finalState());
                    } else {
                        log.warn("Event {} failed for account {}: {}", event.sequenceNumber(), aggregateId, result.reason());
                    }
                    updateOffset(event.sequenceNumber());
                });
                return fetched.isPresent();
            });
            if (Boolean.FALSE.equals(processed)) {
                break;
            }
        }
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

    private void ensureConnection() throws SQLException {
        if (rawConnection == null || rawConnection.isClosed()) {
            rawConnection = dataSource.getConnection();
            rawConnection.setAutoCommit(true);
            pgConnection = rawConnection.unwrap(PGConnection.class);
            try (var stmt = rawConnection.createStatement()) {
                stmt.execute("LISTEN " + CHANNEL);
            }
            log.info("Listening on channel '{}'", CHANNEL);
        }
    }

    private void closeConnection() {
        try {
            if (rawConnection != null && !rawConnection.isClosed()) {
                rawConnection.close();
            }
        } catch (SQLException e) {
            log.warn("Error closing listen connection", e);
        }
        rawConnection = null;
        pgConnection = null;
    }
}
