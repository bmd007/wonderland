package io.github.pubsubseekbucket.publish.statedump;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.Snapshot;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import io.github.pubsubseekbucket.util.Serializer;
import io.github.pubsubseekbucket.util.SubscriptionAdminUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class StateDumpService<T> {
    private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSVV").withZone(ZoneOffset.UTC);

    private final String fileName;
    private final StateDumpHandler stateDumpHandler;
    private final Serializer<T, String> serializer;
    private final Map<String, AtomicLong> gaugeMap = new HashMap<>();
    private final MeterRegistry meterRegistry;
    private final SubscriptionAdminUtil subscriptionAdminUtil;
    private final BiConsumer<Integer, Consumer<T>> supplier;

    public StateDumpService(String fileName,
                            BiConsumer<Integer, Consumer<T>> supplier,
                            Storage storage,
                            ObjectMapper objectMapper,
                            SubscriptionAdminUtil subscriptionAdminUtil,
                            MeterRegistry meterRegistry) {
        this(fileName, supplier, storage, objectMapper::writeValueAsString, subscriptionAdminUtil, meterRegistry);
    }

    public StateDumpService(String fileName,
                            BiConsumer<Integer, Consumer<T>> supplier,
                            Storage storage,
                            Serializer<T, String> serializer,
                            SubscriptionAdminUtil subscriptionAdminUtil,
                            MeterRegistry meterRegistry) {
        this.fileName = fileName;
        this.serializer = serializer;
        this.supplier = supplier;
        this.meterRegistry = meterRegistry;
        this.stateDumpHandler = new StateDumpHandler(storage);
        this.subscriptionAdminUtil = subscriptionAdminUtil;
    }

    public StateDumpResult performStateDump(String topicName, String bucketName, int version) {
        return performStateDump(topicName, bucketName, version, false);
    }

    /**
     * Create PubSub snapshot and perform a full state dump.
     *
     * @param topicName        topic name
     * @param bucketName       target bucket name
     * @param version          topic version
     * @param suppressErrorLog flag for omitting ERROR logs in case of frequent state dumping (e.g. price intakes)
     * @return a state dump result entity
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public StateDumpResult performStateDump(String topicName, String bucketName, int version, boolean suppressErrorLog) {
        String dumpTime = INSTANT_FORMATTER.format(Instant.now().plusMillis(version));
        String dumpFolderName = "dump_" + dumpTime;
        String tmpFileName = "pending_" + dumpTime + "/" + fileName;
        String stateDumpFileName = dumpFolderName + "/" + fileName;

        AtomicLong eventCount = new AtomicLong();
        AtomicLong failedWriteCount = new AtomicLong();
        long before = System.currentTimeMillis();

        BlobInfo tmpStateDumpBlob = stateDumpHandler.createStateDumpBlob(tmpFileName, CONTENT_TYPE, bucketName);

        StateDumpResult.StateDumpResultBuilder resultBuilder = StateDumpResult.builder()
                .snapshotName(dumpFolderName)
                .tmpFile(tmpFileName);

        try (BlobWriterWrapper blobWriter = stateDumpHandler.createStateDumpWriter(tmpStateDumpBlob)) {
            log.info("Starting state dump to tmp blob {} in bucket {}", blobWriter.getBlobInfo().getName(), blobWriter.getBlobInfo().getBucket());

            AtomicLong gaugeTimeStamp = gaugeMap.computeIfAbsent(topicName, createGauge(topicName));
            createSnapshot(topicName, dumpFolderName, gaugeTimeStamp);
            resultBuilder.timeToCreateSnapshot(gaugeTimeStamp.get());

            Consumer<T> dumper = createDumper(blobWriter.getBufferedWriter(), eventCount, failedWriteCount);
            supplier.accept(version, dumper);
        } catch (RuntimeException | IOException e) {
            resultBuilder.failure(e);
            if (suppressErrorLog) {
                log.warn("Failed in state dump creation: {}", tmpStateDumpBlob.getBlobId(), e);
            } else {
                log.error("Failed in state dump creation: {}", tmpStateDumpBlob.getBlobId(), e);
            }
            stateDumpHandler.deleteDump(tmpStateDumpBlob);
            return resultBuilder.build();
        }

        if (failedWriteCount.get() <= 0) {
            BlobInfo stateDumpBlob = stateDumpHandler.renameDump(tmpStateDumpBlob, stateDumpFileName, bucketName);
            recordSuccess(resultBuilder, before, eventCount.get(), stateDumpBlob.getBlobId());
            resultBuilder.stateDumpBlob(stateDumpBlob);
            resultBuilder.file(stateDumpFileName);
        } else {
            recordFailure(resultBuilder, tmpStateDumpBlob, failedWriteCount.get());
            stateDumpHandler.deleteDump(tmpStateDumpBlob);
        }

        return resultBuilder.build();
    }

    private Function<String, AtomicLong> createGauge(String topicName) {
        return key -> {
            AtomicLong time = new AtomicLong(0);
            Gauge.builder("snapshot_creation_time", time, AtomicLong::get)
                    .tag("topic", topicName)
                    .register(meterRegistry);
            return time;
        };
    }

    private void recordFailure(StateDumpResult.StateDumpResultBuilder resultBuilder, BlobInfo tmpStateDumpBlob, long failedWrites) {
        log.error("There were {} errors in processing state dump!", failedWrites);
        resultBuilder.eventWriteErrors(failedWrites).stateDumpBlob(tmpStateDumpBlob);
    }

    private void recordSuccess(StateDumpResult.StateDumpResultBuilder resultBuilder, long before, long numberOfEvents, BlobId blobId) {
        log.info("Completed state dump of {} events in {} seconds to blob {} in bucket {}",
                numberOfEvents,
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - before),
                blobId.getName(),
                blobId.getBucket());

        resultBuilder.success(true).eventsInStateDump(numberOfEvents);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Consumer<T> createDumper(BufferedWriter bw, AtomicLong dumpedEventsCounter, AtomicLong failedWriteCount) {
        return event -> {
            try {
                bw.write(serializer.write(event));
                bw.newLine();
                dumpedEventsCounter.incrementAndGet();
            } catch (RuntimeException | IOException e) {
                final long errorCnt = failedWriteCount.incrementAndGet();
                if (errorCnt < 10) {
                    log.error("Could not write individual event to state dump {}", event, e);
                } else if (errorCnt == 10) {
                    log.error("Reached error logging threshold. Hiding further errors.");
                }
            }
        };
    }

    private void createSnapshot(String topicName, String snapshotName, AtomicLong gaugeTimeStamp) {
        String subscriptionName = topicName + "_" + UUID.randomUUID();
        subscriptionAdminUtil.createSubscription(topicName, subscriptionName, null);

        long timestampStartNanos = System.nanoTime();
        long delayTime = 1000;
        try {
            while (true) {
                try {
                    long before = System.nanoTime();
                    Snapshot snapshot = subscriptionAdminUtil.createSnapshot(subscriptionName, snapshotName, Map.of());
                    log.info("Created snapshot {} for topic {} in {} millis", snapshot.getName(), topicName,
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - before));

                    gaugeTimeStamp.set(getElapsedTimeMillis(timestampStartNanos));
                    return;
                } catch (Exception e) { // NOPMD
                    long elapsedTime = getElapsedTimeMillis(timestampStartNanos);
                    if (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) > 60) {
                        log.warn("Couldn't create snapshot: {} on topic {} within 60 seconds, terminating try", snapshotName, topicName, e);
                        gaugeTimeStamp.set(elapsedTime);
                        throw e;
                    }

                    log.warn("Failed to create snapshot: {} on topic {} will retry again in {} millis, time since start : {} millis, reason: {}",
                            snapshotName, topicName, delayTime, elapsedTime, e.getMessage());

                    try {
                        Thread.sleep(delayTime);
                        delayTime *= 1.5;
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }
        } finally {
            subscriptionAdminUtil.deleteSubscription(subscriptionName);
        }
    }

    private long getElapsedTimeMillis(long timestampStart) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timestampStart);
    }

    @Builder
    @lombok.Value
    public static class StateDumpResult {
        String snapshotName;
        String file;
        String tmpFile;
        @Builder.Default
        boolean success = false;
        Exception failure;
        long eventsInStateDump;
        long eventWriteErrors;
        long timeToCreateSnapshot;
        BlobInfo stateDumpBlob;
    }
}
