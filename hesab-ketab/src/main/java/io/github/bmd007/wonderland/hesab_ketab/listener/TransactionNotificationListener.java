package io.github.bmd007.wonderland.hesab_ketab.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class TransactionNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionNotificationListener.class);
    private static final String CHANNEL = "new_transaction";

    private final DataSource dataSource;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private Connection listenConnection;

    public TransactionNotificationListener(DataSource dataSource,
                                           AccountRepository accountRepository) {
        this.dataSource = dataSource;
        this.accountRepository = accountRepository;
    }

    private Connection getListenConnection() throws SQLException {
        if (listenConnection == null || listenConnection.isClosed()) {
            listenConnection = dataSource.getConnection();
            listenConnection.setAutoCommit(true);
            try (var stmt = listenConnection.createStatement()) {
                stmt.execute("LISTEN " + CHANNEL);
            }
            log.info("Listening on Postgres channel '{}'", CHANNEL);
        }
        return listenConnection;
    }

    @Scheduled(fixedDelay = 200)
    public void pollNotifications() {
        try {
            var conn = getListenConnection();
            // Issue an empty query to receive pending notifications
            try (var stmt = conn.createStatement()) {
                stmt.execute("");
            }
            var pgConn = conn.unwrap(PGConnection.class);
            var notifications = pgConn.getNotifications();
            if (notifications == null) return;
            for (PGNotification notification : notifications) {
                handleNotification(notification.getParameter());
            }
        } catch (Exception e) {
            log.error("Error polling notifications", e);
            closeListenConnection();
        }
    }

    private void handleNotification(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            var fromAccountId = UUID.fromString(node.get("from_account_id").asText());
            var toAccountId = UUID.fromString(node.get("to_account_id").asText());
            var amount = new BigDecimal(node.get("amount").asText());
            accountRepository.updateProjectedBalance(fromAccountId, amount.negate());
            accountRepository.updateProjectedBalance(toAccountId, amount);
            log.debug("Updated projection: {} -> {}, amount {}", fromAccountId, toAccountId, amount);
        } catch (Exception e) {
            log.error("Failed to process transaction notification: {}", payload, e);
        }
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
