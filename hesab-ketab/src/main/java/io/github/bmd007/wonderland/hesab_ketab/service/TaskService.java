package io.github.bmd007.wonderland.hesab_ketab.service;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTaskRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import io.github.bmd007.wonderland.hesab_ketab.repository.ScheduledTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final ScheduledTaskRepository taskRepository;

    public TaskService(ScheduledTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public ScheduledTask schedule(CreateTaskRequest request) {
        return taskRepository.schedule(request);
    }

    @Transactional
    public Optional<ScheduledTask> claimNext() {
        return taskRepository.claimNext();
    }

    @Transactional
    public void complete(UUID id) {
        taskRepository.complete(id);
    }

    @Transactional
    public void fail(UUID id, String error) {
        taskRepository.fail(id, error);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> findPending() {
        return taskRepository.findPending();
    }
}
