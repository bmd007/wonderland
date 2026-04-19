package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountBalance;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateAccountRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.LedgerException;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@RequestBody CreateAccountRequest request) {
        return accountRepository.create(request);
    }

    @GetMapping("/{id}")
    public AccountBalance findById(@PathVariable UUID id) {
        return accountRepository.findBalanceById(id)
            .orElseThrow(() -> new LedgerException.AccountNotFound(id));
    }

    @GetMapping
    public List<AccountBalance> findAll() {
        return accountRepository.findAllBalances();
    }
}
