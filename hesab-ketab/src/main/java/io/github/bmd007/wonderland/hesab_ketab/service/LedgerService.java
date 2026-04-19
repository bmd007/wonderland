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
        // Compute balance from transaction history (the source of truth)
        var fromBalance = accountRepository.findBalanceById(request.fromAccountId())
            .orElseThrow(() -> new LedgerException.AccountNotFound(request.fromAccountId()))
            .balance();
        if (fromBalance.compareTo(request.amount()) < 0) {
            throw new LedgerException.InsufficientBalance(from.id(), request.amount(), fromBalance);
        }
        // Append the event — no balance mutation
        return transactionRepository.create(request, from.currency());
    }

    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
