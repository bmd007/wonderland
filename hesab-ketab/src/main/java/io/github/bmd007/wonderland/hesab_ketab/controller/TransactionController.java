package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.Account;
import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTransactionRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.TransferRecord;
import io.github.bmd007.wonderland.hesab_ketab.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final LedgerService ledgerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account transfer(@RequestBody CreateTransactionRequest request) {
        return ledgerService.transfer(request);
    }

    @GetMapping("/account/{accountId}")
    public List<TransferRecord> findByAccountId(@PathVariable UUID accountId) {
        return ledgerService.findTransfersForAccount(accountId);
    }
}
