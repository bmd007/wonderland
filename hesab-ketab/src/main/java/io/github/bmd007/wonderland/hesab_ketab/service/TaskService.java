package io.github.bmd007.wonderland.hesab_ketab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bmd007.wonderland.hesab_ketab.domain.AccountAggregate;
import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import io.github.bmd007.wonderland.hesab_ketab.repository.AccountRepository;
import io.github.bmd007.wonderland.hesab_ketab.repository.EventStore;
import io.github.bmd007.wonderland.hesab_ketab.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final ScheduledTaskRepository taskRepository;
    private final EventStore eventStore;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void scheduleBalanceCheck(UUID accountId, long eventSequence) {
        var payload = serialize(Map.of("accountId", accountId.toString()));
        var deterministicId = UUID.nameUUIDFromBytes(("BALANCE_CHECK:" + eventSequence).getBytes());
        taskRepository.scheduleIdempotent(deterministicId, "BALANCE_CHECK", payload);
    }

    @Scheduled(fixedDelay = 1000)
    public void processNextTask() {
        taskRepository.claimNext()
            .ifPresent(task -> {
                try {
                    execute(task);
                    taskRepository.complete(task.id());
                } catch (Exception e) {
                    log.error("Task {} failed", task.id(), e);
                    taskRepository.fail(task.id(), e.getMessage());
                }
            });
    }

    @Transactional
    public void triggerFullConsistencyCheck() {
        var batchKey = UUID.randomUUID().toString();
        accountRepository.findAll().forEach(account -> {
            var payload = serialize(Map.of("accountId", account.id().toString()));
            var deterministicId = UUID.nameUUIDFromBytes(("BALANCE_CHECK:manual:" + batchKey + ":" + account.id()).getBytes());
            taskRepository.scheduleIdempotent(deterministicId, "BALANCE_CHECK", payload);
        });
        log.info("Scheduled balance checks for all accounts");
    }

    public List<ScheduledTask> findAll() {
        return taskRepository.findAll();
    }

    public List<ScheduledTask> findFailed() {
        return taskRepository.findFailed();
    }

    private void execute(ScheduledTask task) {
        switch (task.taskType()) {
            case "BALANCE_CHECK" -> executeBalanceCheck(task);
            default -> log.warn("Unknown task type: {}", task.taskType());
        }
    }

    private void executeBalanceCheck(ScheduledTask task) {
        var node = deserialize(task.payload());
        var accountId = UUID.fromString(node.get("accountId").asText());
        var events = eventStore.loadEvents(accountId);
        var reconstituted = AccountAggregate.reconstitute(events);
        accountRepository.findById(accountId)
            .ifPresent(snapshot -> {
                if (snapshot.balance().compareTo(reconstituted.balance()) != 0) {
                    log.warn("Balance mismatch for {}: snapshot={}, events={}. Repairing.",
                        accountId, snapshot.balance(), reconstituted.balance());
                    accountRepository.save(reconstituted.toSnapshot());
                }
            });
    }

    private String serialize(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private com.fasterxml.jackson.databind.JsonNode deserialize(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
