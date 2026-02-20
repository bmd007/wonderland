package io.github.pubsubseekbucket.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableScheduling
public class EventBusScheduler implements SchedulingConfigurer {
    private final Collection<TriggerTask> pendingTasks = new ArrayList<>();
    private ScheduledTaskRegistrar taskRegistrar;

    public void addTriggerTask(TriggerTask task) {
        synchronized (this) {
            if (taskRegistrar != null)
                taskRegistrar.addTriggerTask(task);
            else
                pendingTasks.add(task);
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        synchronized (this) {
            if (this.taskRegistrar != null)
                throw new IllegalStateException("Task registrar already configured");
            this.taskRegistrar = taskRegistrar;
            pendingTasks.forEach(taskRegistrar::addTriggerTask);
            pendingTasks.clear();
        }
    }
}
