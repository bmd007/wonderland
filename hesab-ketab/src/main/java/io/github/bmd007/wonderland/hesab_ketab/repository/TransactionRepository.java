package io.github.bmd007.wonderland.hesab_ketab.repository;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTransactionRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.Transaction;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class TransactionRepository {

    private final JdbcClient jdbc;

    public TransactionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Transaction create(CreateTransactionRequest request, String currency) {
        var id = UUID.randomUUID();
        return jdbc.sql("""
                INSERT INTO transactions (id, from_account_id, to_account_id, amount, currency, description)
                VALUES (:id, :fromAccountId, :toAccountId, :amount, :currency, :description)
                RETURNING id, from_account_id, to_account_id, amount, currency, description, created_at
                """)
            .param("id", id)
            .param("fromAccountId", request.fromAccountId())
            .param("toAccountId", request.toAccountId())
            .param("amount", request.amount())
            .param("currency", currency)
            .param("description", request.description())
            .query(Transaction.class)
            .single();
    }

    public List<Transaction> findByAccountId(UUID accountId) {
        return jdbc.sql("""
                SELECT id, from_account_id, to_account_id, amount, currency, description, created_at
                FROM transactions
                WHERE from_account_id = :accountId OR to_account_id = :accountId
                ORDER BY created_at DESC
                """)
            .param("accountId", accountId)
            .query(Transaction.class)
            .list();
    }
}
