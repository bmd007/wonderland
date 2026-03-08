package io.github.pubsubseekbucket.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventBusSchedulerTest {

    @InjectMocks
    private EventBusScheduler target;

    @Test
    void addTriggerTaskBeforeConfigure() {
        TriggerTask task = new TriggerTask(() -> {
        }, new PeriodicTrigger(Duration.ofSeconds(3)));
        target.addTriggerTask(task);

        ScheduledTaskRegistrar registrar = mock(ScheduledTaskRegistrar.class);
        target.configureTasks(registrar);

        verify(registrar).addTriggerTask(task);
    }

    @Test
    void addTriggerTaskAfterConfigure() {
        TriggerTask task = new TriggerTask(() -> {
        }, new PeriodicTrigger(Duration.ofSeconds(3)));

        ScheduledTaskRegistrar registrar = mock(ScheduledTaskRegistrar.class);
        target.configureTasks(registrar);

        target.addTriggerTask(task);

        verify(registrar).addTriggerTask(task);
    }
}
