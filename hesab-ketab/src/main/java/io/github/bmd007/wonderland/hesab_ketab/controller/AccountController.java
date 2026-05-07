package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.DomainEvent;
import io.github.bmd007.wonderland.hesab_ketab.dto.CreateAccountRequest;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
class AccountController {

    AccountRepository accountRepository;
    EventStore eventStore;

    @GetMapping("/{id}")
    Account findById(@PathVariable UUID id) {
        return accountRepository.findById(id).orElseThrow();
    }

    @GetMapping
    List<Account> findAll() {
        return accountRepository.findAll();
    }

    @GetMapping("/{id}/events")
    List<DomainEvent> getEventHistory(@PathVariable UUID id) {
        return eventStore.loadEvents(id);
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
}
