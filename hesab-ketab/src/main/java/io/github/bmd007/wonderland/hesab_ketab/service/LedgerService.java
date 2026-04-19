package io.github.bmd007.wonderland.hesab_ketab.service;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTransactionRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.LedgerException;
import io.github.bmd007.wonderland.hesab_ketab.domain.Transaction;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LedgerService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transfer(CreateTransactionRequest request) {
        // Lock account rows to serialize concurrent transfers
        var from = accountRepository.findByIdForUpdate(request.fromAccountId())
            .orElseThrow(() -> new LedgerException.AccountNotFound(request.fromAccountId()));
        var to = accountRepository.findByIdForUpdate(request.toAccountId())
            .orElseThrow(() -> new LedgerException.AccountNotFound(request.toAccountId()));
        if (!from.currency().equals(to.currency())) {
            throw new LedgerException.IncompatibleCurrencies(from.currency(), to.currency());
        }
        // Validate against the event store (source of truth), not the projection
        var fromBalance = transactionRepository.computeBalance(request.fromAccountId());
        if (fromBalance.compareTo(request.amount()) < 0) {
            throw new LedgerException.InsufficientBalance(from.id(), request.amount(), fromBalance);
        }
        // Append event — the trigger fires pg_notify, the listener updates the projection
        return transactionRepository.create(request, from.currency());
    }

    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
