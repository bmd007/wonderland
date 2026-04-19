package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountBalance;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateAccountRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepository {

    private final JdbcClient jdbc;

    public AccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Account create(CreateAccountRequest request) {
        var id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO accounts (id, name, currency)
                VALUES (:id, :name, :currency)
                """)
            .param("id", id)
            .param("name", request.name())
            .param("currency", request.currency())
            .update();
        jdbc.sql("""
                INSERT INTO account_balances (account_id, balance)
                VALUES (:id, 0)
                """)
            .param("id", id)
            .update();
        return new Account(id, request.name(), request.currency(), null);
    }

    public Optional<Account> findById(UUID id) {
        return jdbc.sql("SELECT id, name, currency, created_at FROM accounts WHERE id = :id")
            .param("id", id)
            .query(Account.class)
            .optional();
    }

    // Pessimistic lock on account row to serialize concurrent transfers
    public Optional<Account> findByIdForUpdate(UUID id) {
        return jdbc.sql("SELECT id, name, currency, created_at FROM accounts WHERE id = :id FOR UPDATE")
            .param("id", id)
            .query(Account.class)
            .optional();
    }

    // Read from projection table (eventually consistent, maintained by LISTEN/NOTIFY listener)
    public Optional<AccountBalance> findBalanceById(UUID id) {
        return jdbc.sql("""
                SELECT a.id, a.name, a.currency, a.created_at, b.balance
                FROM accounts a
                JOIN account_balances b ON b.account_id = a.id
                WHERE a.id = :id
                """)
            .param("id", id)
            .query(AccountBalance.class)
            .optional();
    }

    public List<AccountBalance> findAllBalances() {
        return jdbc.sql("""
                SELECT a.id, a.name, a.currency, a.created_at, b.balance
                FROM accounts a
                JOIN account_balances b ON b.account_id = a.id
                ORDER BY a.created_at DESC
                """)
            .query(AccountBalance.class)
            .list();
    }

    public void updateProjectedBalance(UUID accountId, BigDecimal delta) {
        jdbc.sql("""
                UPDATE account_balances
                SET balance = balance + :delta, updated_at = now()
                WHERE account_id = :accountId
                """)
            .param("delta", delta)
            .param("accountId", accountId)
            .update();
    }
}
