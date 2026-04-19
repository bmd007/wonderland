package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.CreateTaskRequest;
import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import io.github.bmd007.wonderland.hesab_ketab.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduledTask schedule(@RequestBody CreateTaskRequest request) {
        return taskService.schedule(request);
    }

    @PostMapping("/claim")
    public ResponseEntity<ScheduledTask> claimNext() {
        return taskService.claimNext()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/complete")
    public void complete(@PathVariable UUID id) {
        taskService.complete(id);
    }

    @PostMapping("/{id}/fail")
    public void fail(@PathVariable UUID id, @RequestBody String error) {
        taskService.fail(id, error);
    }

    @GetMapping("/pending")
    public List<ScheduledTask> findPending() {
        return taskService.findPending();
    }
}
