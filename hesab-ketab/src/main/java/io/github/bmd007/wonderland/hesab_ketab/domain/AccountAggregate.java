package io.github.bmd007.wonderland.hesab_ketab.domain;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class AccountAggregate {

    private UUID id;
    private String name;
    private BigDecimal balance = BigDecimal.ZERO;
    private long version = 0;
    private final List<AccountEvent> uncommittedEvents = new ArrayList<>();

    public static AccountAggregate open(String name) {
        var aggregate = new AccountAggregate();
        aggregate.emit(new AccountEvent.AccountOpened(UUID.randomUUID(), name, Instant.now()));
        return aggregate;
    }

    public static AccountAggregate reconstitute(List<AccountEvent> events) {
        var aggregate = new AccountAggregate();
        events.forEach(aggregate::apply);
        return aggregate;
    }

    public static AccountAggregate fromSnapshot(Account snapshot) {
        var aggregate = new AccountAggregate();
        aggregate.id = snapshot.id();
        aggregate.name = snapshot.name();
        aggregate.balance = snapshot.balance();
        aggregate.version = snapshot.version();
        return aggregate;
    }

    public void debit(BigDecimal amount, UUID transactionId) {
        if (balance.compareTo(amount) < 0) {
            throw new LedgerException.InsufficientBalance(id, amount, balance);
        }
        emit(new AccountEvent.MoneyDebited(id, amount, transactionId, Instant.now()));
    }

    public void credit(BigDecimal amount, UUID transactionId) {
        emit(new AccountEvent.MoneyCredited(id, amount, transactionId, Instant.now()));
    }

    public void deposit(BigDecimal amount) {
        emit(new AccountEvent.MoneyDeposited(id, amount, Instant.now()));
    }

    private void apply(AccountEvent event) {
        switch (event) {
            case AccountEvent.AccountOpened e -> {
                this.id = e.accountId();
                this.name = e.name();
                this.balance = BigDecimal.ZERO;
            }
            case AccountEvent.MoneyDeposited e -> this.balance = this.balance.add(e.amount());
            case AccountEvent.MoneyDebited e -> this.balance = this.balance.subtract(e.amount());
            case AccountEvent.MoneyCredited e -> this.balance = this.balance.add(e.amount());
        }
        this.version++;
    }

    private void emit(AccountEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    public List<AccountEvent> uncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }

    public Account toSnapshot() {
        return Account.builder()
            .id(id)
            .name(name)
            .balance(balance)
            .version(version)
            .build();
    }

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
}
