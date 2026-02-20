package io.github.pubsubseekbucket.subscribe.statedump;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

@Slf4j
public class ProgressLogger<RecordType> implements Consumer<RecordType> {
    private final Duration logInterval;
    private final String recordType;
    private final long startOfProcessingTimeNanos;

    private long nextLogTimeNanos;
    private long recordsProcessedAtLastLogTime = 0;
    private long recordsProcessed = 0;
    private long latestTimeNanos;

    public ProgressLogger(Duration logInterval, String recordType) {
        this.logInterval = logInterval;
        this.recordType = recordType;
        startOfProcessingTimeNanos = latestTimeNanos = System.nanoTime();
        nextLogTimeNanos = startOfProcessingTimeNanos + this.logInterval.toNanos();
    }

    @Override
    public void accept(RecordType object) {
        recordsProcessed = getRecordsProcessed() + 1;

        latestTimeNanos = System.nanoTime();
        if (latestTimeNanos > nextLogTimeNanos) {
            logProgress(logInterval.toSeconds());
            nextLogTimeNanos = latestTimeNanos + logInterval.toNanos();
            recordsProcessedAtLastLogTime = getRecordsProcessed();
        }
    }

    public void postProcessing() {
        var previousTimeNanos = latestTimeNanos;
        latestTimeNanos = System.nanoTime();
        if (getRecordsProcessed() > recordsProcessedAtLastLogTime) {
            logProgress(Duration.of(latestTimeNanos - previousTimeNanos, ChronoUnit.NANOS).toSeconds());
        }
        var executionTimeInSeconds = getExecutionTimeInSeconds();
        log.info("Finished processing {} {} in {} seconds and an average rate of {} per second.",
                getRecordsProcessed(),
                recordType,
                executionTimeInSeconds,
                (long) executionTimeInSeconds > 0 ? getRecordsProcessed() / (long) executionTimeInSeconds : "n/a");
    }

    private void logProgress(long lastIntervalSeconds) {
        log.info("Processed {} {} at a rate of {} per second.",
                getRecordsProcessed(),
                recordType,
                lastIntervalSeconds > 0 ? (getRecordsProcessed() - recordsProcessedAtLastLogTime) / lastIntervalSeconds : "n/a");
    }

    public long getRecordsProcessed() {
        return recordsProcessed;
    }

    public double getExecutionTimeInSeconds() {
        return (double) Duration.of(latestTimeNanos - startOfProcessingTimeNanos, ChronoUnit.NANOS).toMillis() / 1000;
    }
}
