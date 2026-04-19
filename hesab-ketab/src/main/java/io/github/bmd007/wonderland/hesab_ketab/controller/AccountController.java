package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateAccountRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.LedgerException;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@RequestBody CreateAccountRequest request) {
        return ledgerService.openAccount(request);
    }

    @GetMapping("/{id}")
    public Account findById(@PathVariable UUID id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new LedgerException.AccountNotFound(id));
    }

    @GetMapping
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @PostMapping("/{id}/deposit")
    public Account deposit(@PathVariable UUID id, @RequestBody BigDecimal amount) {
        return ledgerService.deposit(id, amount);
    }

    @GetMapping("/{id}/events")
    public List<AccountEvent> getEventHistory(@PathVariable UUID id) {
        return ledgerService.getEventHistory(id);
    }
}
