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
    public void triggerFullProjectionRebuild() {
        eventStore.findAllAggregateIds().forEach(aggregateId -> {
            var payload = serialize(Map.of("accountId", aggregateId.toString()));
            var deterministicId = UUID.nameUUIDFromBytes(("PROJECTION_REBUILD:" + aggregateId).getBytes());
            taskRepository.scheduleIdempotent(deterministicId, "PROJECTION_REBUILD", payload);
        });
        log.info("Scheduled projection rebuild for all aggregates");
    }

    public List<ScheduledTask> findAll() {
        return taskRepository.findAll();
    }

    public List<ScheduledTask> findFailed() {
        return taskRepository.findFailed();
    }

    private void execute(ScheduledTask task) {
        switch (task.taskType()) {
            case "PROJECTION_REBUILD" -> executeProjectionRebuild(task);
            default -> log.warn("Unknown task type: {}", task.taskType());
        }
    }

    private void executeProjectionRebuild(ScheduledTask task) {
        var node = deserialize(task.payload());
        var accountId = UUID.fromString(node.get("accountId").asText());
        var events = eventStore.loadEvents(accountId);
        if (!events.isEmpty()) {
            var aggregate = AccountAggregate.reconstitute(events);
            accountRepository.save(aggregate.toSnapshot());
            log.info("Projection rebuilt for {}: balance={}", accountId, aggregate.balance());
        }
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
