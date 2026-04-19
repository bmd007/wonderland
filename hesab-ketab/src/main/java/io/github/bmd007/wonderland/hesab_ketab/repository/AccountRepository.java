package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final JdbcClient jdbc;

    public void save(Account snapshot) {
        jdbc.sql("""
                INSERT INTO accounts (id, name, balance, version)
                VALUES (:id, :name, :balance, :version)
                ON CONFLICT (id) DO UPDATE
                SET balance = :balance, version = :version
                """)
            .param("id", snapshot.id())
            .param("name", snapshot.name())
            .param("balance", snapshot.balance())
            .param("version", snapshot.version())
            .update();
    }

    public Optional<Account> findById(UUID id) {
        return jdbc.sql("SELECT id, name, balance, version, created_at FROM accounts WHERE id = :id")
            .param("id", id)
            .query(Account.class)
            .optional();
    }

    public Optional<Account> findByIdForUpdate(UUID id) {
        return jdbc.sql("SELECT id, name, balance, version, created_at FROM accounts WHERE id = :id FOR UPDATE")
            .param("id", id)
            .query(Account.class)
            .optional();
    }

    public List<Account> findAll() {
        return jdbc.sql("SELECT id, name, balance, version, created_at FROM accounts ORDER BY created_at DESC")
            .query(Account.class)
            .list();
    }
}
