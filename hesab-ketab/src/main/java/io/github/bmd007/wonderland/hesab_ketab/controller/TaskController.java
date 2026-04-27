package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.ScheduledTask;
import io.github.bmd007.wonderland.hesab_ketab.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/projection-rebuild")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void triggerProjectionRebuild() {
        taskService.triggerFullProjectionRebuild();
    }
}
