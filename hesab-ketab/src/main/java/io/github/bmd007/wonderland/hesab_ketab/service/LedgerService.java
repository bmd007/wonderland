package io.github.bmd007.wonderland.hesab_ketab.service;

import io.github.bmd007.wonderland.hesab_ketab.domain.*;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final EventStore eventStore;

    @Transactional
    public Account openAccount(CreateAccountRequest request) {
        var aggregate = AccountAggregate.open(request.name());
        saveAggregate(aggregate);
        return aggregate.toSnapshot();
    }

    @Transactional
    public Account deposit(UUID accountId, BigDecimal amount) {
        var aggregate = loadAggregate(accountId);
        aggregate.deposit(amount);
        saveAggregate(aggregate);
        return aggregate.toSnapshot();
    }

    @Transactional
    public Account transfer(CreateTransactionRequest request) {
        var from = loadAggregate(request.fromAccountId());
        var to = loadAggregate(request.toAccountId());
        var txnId = UUID.randomUUID();
        from.debit(request.amount(), txnId);
        to.credit(request.amount(), txnId);
        saveAggregate(from);
        saveAggregate(to);
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

    private AccountAggregate loadAggregate(UUID id) {
        var snapshot = accountRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new LedgerException.AccountNotFound(id));
        return AccountAggregate.fromSnapshot(snapshot);
    }

    private void saveAggregate(AccountAggregate aggregate) {
        long baseVersion = aggregate.version() - aggregate.uncommittedEvents().size();
        eventStore.append(aggregate.id(), aggregate.uncommittedEvents(), baseVersion);
        accountRepository.save(aggregate.toSnapshot());
        aggregate.markEventsAsCommitted();
    }
}
