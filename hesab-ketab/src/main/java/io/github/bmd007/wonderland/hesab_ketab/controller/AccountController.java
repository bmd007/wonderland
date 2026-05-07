package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.domain.DomainEvent;
import io.github.bmd007.wonderland.hesab_ketab.dto.AccountStatement;
import io.github.bmd007.wonderland.hesab_ketab.dto.CreateAccountRequest;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
class AccountController {

    private final AccountRepository accountRepository;
    private final EventStore eventStore;

    @GetMapping
    List<Account> findAll() {
        return accountRepository.findAll();
    }

    @GetMapping("/{id}")
    Account findById(@PathVariable UUID id) {
        return accountRepository.findById(id).orElseThrow();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Account create(@RequestBody CreateAccountRequest request) {
        var account = Account.builder()
            .name(request.name())
            .createdAt(Instant.now())
            .balance(BigDecimal.ZERO)
            .id(UUID.randomUUID())
            .build();
        accountRepository.save(account);
        return account;
    }

    @GetMapping("/{id}/events")
    List<DomainEvent> getEventHistory(@PathVariable UUID id) {
        return eventStore.loadEvents(id);
    }

    @GetMapping("/{id}/balance")
    Account getBalanceAsOf(@PathVariable UUID id, @RequestParam Instant asOf) {
        var account = accountRepository.findById(id).orElseThrow();
        var events = eventStore.loadEventsUpTo(id, asOf);
        var replayed = Account.builder()
            .id(account.id())
            .name(account.name())
            .balance(BigDecimal.ZERO)
            .version(0)
            .createdAt(account.createdAt())
            .build();
        for (var event : events) {
            var result = replayed.apply(event);
            replayed = result.finalState();
        }
        return replayed;
    }

    @GetMapping("/{id}/statement")
    AccountStatement getStatement(@PathVariable UUID id, @RequestParam Instant from, @RequestParam Instant to) {
        var account = accountRepository.findById(id).orElseThrow();
        var eventsBeforeFrom = eventStore.loadEventsUpTo(id, from);
        var openingBalance = BigDecimal.ZERO;
        for (var event : eventsBeforeFrom) {
            openingBalance = applyToBalance(openingBalance, event);
        }
        var eventsBetween = eventStore.loadEventsBetween(id, from, to);
        var entries = new ArrayList<AccountStatement.Entry>();
        var runningBalance = openingBalance;
        for (var event : eventsBetween) {
            var debit = BigDecimal.ZERO;
            var credit = BigDecimal.ZERO;
            var description = "";
            switch (event) {
                case AccountEvent.MoneyDebited debited -> {
                    debit = debited.amount();
                    description = "Transfer out (txn " + debited.transactionId().toString().substring(0, 8) + ")";
                }
                case AccountEvent.MoneyCredited credited -> {
                    credit = credited.amount();
                    description = "Transfer in (txn " + credited.transactionId().toString().substring(0, 8) + ")";
                }
                default -> description = event.type();
            }
            runningBalance = runningBalance.subtract(debit).add(credit);
            entries.add(new AccountStatement.Entry(event.occurredAt(), description, debit, credit, runningBalance));
        }
        return new AccountStatement(account.name(), from, to, openingBalance, runningBalance, entries);
    }

    private BigDecimal applyToBalance(BigDecimal balance, DomainEvent event) {
        return switch (event) {
            case AccountEvent.MoneyDebited debited -> balance.subtract(debited.amount());
            case AccountEvent.MoneyCredited credited -> balance.add(credited.amount());
            default -> balance;
        };
    }
}
