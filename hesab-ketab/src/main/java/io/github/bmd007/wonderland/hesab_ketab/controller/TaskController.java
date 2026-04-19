package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import io.github.bmd007.wonderland.hesab_ketab.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<ScheduledTask> findAll() {
        return taskService.findAll();
    }

    @GetMapping("/failed")
    public List<ScheduledTask> findFailed() {
        return taskService.findFailed();
    }

    @PostMapping("/consistency-check")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void triggerConsistencyCheck() {
        taskService.triggerFullConsistencyCheck();
    }
}
