package io.github.pubsubseekbucket.publish.persisted;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public class TestData {

    private static final AtomicLong ID_SOURCE = new AtomicLong(1);

    public static long nextUniqueId() {
        return ID_SOURCE.getAndIncrement();
    }

    public static List<Long> getUniqueIds(int cnt) {
        return LongStream.range(ID_SOURCE.get(), ID_SOURCE.addAndGet(cnt)).boxed().toList();
    }

    @NotNull
    public static TestData.SampleEnvelope createEnvelope(long id) {
        return new TestData.SampleEnvelope("JUNIT_" + id, id, "DUMMY",
                new TestData.SampleEvent("TEST_" + id, id));
    }

    public record SampleEnvelope(String entityId, long sequenceNumber, String action, SampleEvent sampleEvent) {
    }

    public record SampleEvent(String field, long anotherField) {
    }
}
