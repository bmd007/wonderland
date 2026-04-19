package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTransactionRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.Transaction;
import io.github.bmd007.wonderland.hesab_ketab.service.LedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction transfer(@RequestBody CreateTransactionRequest request) {
        return ledgerService.transfer(request);
    }

    @GetMapping("/account/{accountId}")
    public List<Transaction> findByAccountId(@PathVariable UUID accountId) {
        return ledgerService.findTransactionsByAccountId(accountId);
    }
}
