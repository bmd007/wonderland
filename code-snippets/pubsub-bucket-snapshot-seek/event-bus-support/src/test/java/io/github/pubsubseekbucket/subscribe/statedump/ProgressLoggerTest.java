package io.github.pubsubseekbucket.subscribe.statedump;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProgressLoggerTest {

    @Test
    void log() throws InterruptedException {
        // Given
        var progressLogger = new ProgressLogger<String>(Duration.of(1, ChronoUnit.MILLIS), "string");

        // When
        progressLogger.accept("A");
        var t1 = progressLogger.getExecutionTimeInSeconds();
        Thread.sleep(1200L);
        progressLogger.accept("B");
        progressLogger.accept("C");

        progressLogger.postProcessing();

        // Then
        var t2 = progressLogger.getExecutionTimeInSeconds();
        assertTrue(t2 >= t1 + 1.0);
        assertEquals(3, progressLogger.getRecordsProcessed());

    }
}
