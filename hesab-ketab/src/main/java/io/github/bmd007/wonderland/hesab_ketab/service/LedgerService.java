package io.github.bmd007.wonderland.hesab_ketab.service;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountAggregate;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountStatement;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateAccountRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTransactionRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.LedgerException;
import io.github.bmd007.wonderland.hesab_ketab.domain.TransferRecord;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final EventStore eventStore;

    @Transactional
    public Account openAccount(CreateAccountRequest request) {
        var aggregate = AccountAggregate.open(request.name());
        saveEvents(aggregate);
        return aggregate.toSnapshot();
    }

    @Transactional
    public Account deposit(UUID accountId, BigDecimal amount) {
        var aggregate = loadAggregate(accountId);
        aggregate.deposit(amount);
        saveEvents(aggregate);
        return aggregate.toSnapshot();
    }

    @Transactional
    public Account transfer(CreateTransactionRequest request) {
        var from = loadAggregate(request.fromAccountId());
        var to = loadAggregate(request.toAccountId());
        var txnId = UUID.randomUUID();
        from.debit(request.amount(), txnId);
        to.credit(request.amount(), txnId);
        saveEvents(from);
        saveEvents(to);
        return from.toSnapshot();
    }

    @Transactional(readOnly = true)
    public List<AccountEvent> getEventHistory(UUID accountId) {
        return eventStore.loadEvents(accountId);
    }

    @Transactional(readOnly = true)
    public List<TransferRecord> findTransfersForAccount(UUID accountId) {
        return eventStore.findTransfersForAccount(accountId);
    }

    @Transactional(readOnly = true)
    public Account getBalanceAsOf(UUID accountId, Instant asOf) {
        var events = eventStore.loadEventsUpTo(accountId, asOf);
        if (events.isEmpty()) {
            throw new LedgerException.AccountNotFound(accountId);
        }
        return AccountAggregate.reconstitute(events).toSnapshot();
    }

    @Transactional(readOnly = true)
    public AccountStatement getStatement(UUID accountId, Instant from, Instant to) {
        var priorEvents = eventStore.loadEventsUpTo(accountId, from);
        if (priorEvents.isEmpty()) {
            throw new LedgerException.AccountNotFound(accountId);
        }
        var priorAggregate = AccountAggregate.reconstitute(priorEvents);
        var openingBalance = priorAggregate.balance();
        var accountName = priorAggregate.name();
        var periodEvents = eventStore.loadEventsBetween(accountId, from, to);
        var entries = new ArrayList<AccountStatement.StatementEntry>();
        var runningBalance = openingBalance;
        for (var event : periodEvents) {
            var debit = BigDecimal.ZERO;
            var credit = BigDecimal.ZERO;
            var description = switch (event) {
                case AccountEvent.MoneyDeposited e -> {
                    credit = e.amount();
                    yield "Deposit";
                }
                case AccountEvent.MoneyDebited e -> {
                    debit = e.amount();
                    yield "Transfer out (txn " + e.transactionId().toString().substring(0, 8) + ")";
                }
                case AccountEvent.MoneyCredited e -> {
                    credit = e.amount();
                    yield "Transfer in (txn " + e.transactionId().toString().substring(0, 8) + ")";
                }
                case AccountEvent.AccountOpened _ -> "Account opened";
            };
            runningBalance = runningBalance.add(credit).subtract(debit);
            entries.add(new AccountStatement.StatementEntry(
                event.occurredAt(), description, debit, credit, runningBalance
            ));
        }
        return new AccountStatement(accountId, accountName, entries, openingBalance, runningBalance);
    }

    private AccountAggregate loadAggregate(UUID id) {
        var events = eventStore.loadEvents(id);
        if (events.isEmpty()) {
            throw new LedgerException.AccountNotFound(id);
        }
        return AccountAggregate.reconstitute(events);
    }

    private void saveEvents(AccountAggregate aggregate) {
        long baseVersion = aggregate.version() - aggregate.uncommittedEvents().size();
        eventStore.append(aggregate.id(), aggregate.uncommittedEvents(), baseVersion);
        aggregate.markEventsAsCommitted();
    }
}
