package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountBalance;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateAccountRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
        return jdbc.sql("""
                INSERT INTO accounts (id, name, currency)
                VALUES (:id, :name, :currency)
                RETURNING id, name, currency, created_at
                """)
            .param("id", id)
            .param("name", request.name())
            .param("currency", request.currency())
            .query(Account.class)
            .single();
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

    // Balance from the live view (always consistent, computed from events)
    public Optional<AccountBalance> findBalanceById(UUID id) {
        return jdbc.sql("SELECT id, name, currency, created_at, balance FROM account_balances WHERE id = :id")
            .param("id", id)
            .query(AccountBalance.class)
            .optional();
    }

    // All balances from the live view
    public List<AccountBalance> findAllBalances() {
        return jdbc.sql("SELECT id, name, currency, created_at, balance FROM account_balances ORDER BY created_at DESC")
            .query(AccountBalance.class)
            .list();
    }

    // Fast read from materialized view (eventually consistent)
    public List<AccountBalance> findAllBalancesCached() {
        return jdbc.sql("SELECT id, name, currency, created_at, balance FROM account_balances_cached ORDER BY created_at DESC")
            .query(AccountBalance.class)
            .list();
    }

    public void refreshBalancesCache() {
        jdbc.sql("SELECT refresh_account_balances_cache()").query().optionalValue();
    }
}
