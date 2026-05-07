package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.AccountEvent;
import io.github.bmd007.wonderland.hesab_ketab.dto.TransferRequest;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final EventStore eventStore;
    private final AccountRepository accountRepository;

    @Transactional
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID transfer(@RequestBody TransferRequest request) {
        var from = accountRepository.findById(request.fromAccountId()).orElseThrow();
        if (from.balance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("not enough money");
        }
        var to = accountRepository.findById(request.toAccountId()).orElseThrow();
        var transactionId = UUID.randomUUID();
        var now = Instant.now();
        var withdrawEvent = AccountEvent.MoneyDebited.builder()
            .atAccountVersion(from.version())
            .transactionId(transactionId)
            .occurredAt(now)
            .amount(request.amount())
            .accountId(from.id())
            .id(UUID.randomUUID())
            .build();
        var depositEvent = AccountEvent.MoneyCredited.builder()
            .atAccountVersion(to.version())
            .transactionId(transactionId)
            .occurredAt(now)
            .amount(request.amount())
            .id(UUID.randomUUID())
            .accountId(to.id())
            .build();
        eventStore.append(withdrawEvent, depositEvent);
        return transactionId;
    }
}
