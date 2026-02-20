package io.github.pubsubseekbucket.subscribe.statedump;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import io.github.pubsubseekbucket.util.Deserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import static com.google.cloud.storage.Storage.BlobListOption.currentDirectory;
import static com.google.cloud.storage.Storage.BlobListOption.prefix;
import static java.util.Comparator.comparing;

@Slf4j
public class StateDumpReader<EventType> {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private static final String DEFAULT_DUMP_PREFIX = "dump";

    private final String bucketName;
    private final Storage storage;
    private final Deserializer<String, EventType> deserializer;
    private final int bufferSize;
    private final String dumpPrefix;

    public StateDumpReader(String bucketName,
                           Class<? extends EventType> eventType,
                           Storage storage,
                           ObjectMapper objectMapper) {
        this(bucketName, eventType, storage, objectMapper, DEFAULT_BUFFER_SIZE);
    }

    public StateDumpReader(String bucketName,
                           Class<? extends EventType> eventType,
                           Storage storage,
                           ObjectMapper objectMapper,
                           int bufferSize) {
        this(bucketName, storage, str -> objectMapper.readValue(str, eventType), bufferSize);
    }

    public StateDumpReader(String bucketName,
                           Storage storage,
                           Deserializer<String, EventType> deserializer) {
        this(bucketName, storage, deserializer, DEFAULT_BUFFER_SIZE);
    }

    public StateDumpReader(String bucketName,
                           Storage storage,
                           Deserializer<String, EventType> deserializer,
                           int bufferSize) {
        this(bucketName, deserializer, storage, bufferSize, DEFAULT_DUMP_PREFIX);
    }

    public StateDumpReader(String bucketName,
                           Class<? extends EventType> eventType,
                           Storage storage,
                           ObjectMapper objectMapper,
                           String dumpPrefix) {
        this(bucketName, str -> objectMapper.readValue(str, eventType), storage, DEFAULT_BUFFER_SIZE, dumpPrefix);
    }

    public StateDumpReader(String bucketName,
                           Deserializer<String, EventType> deserializer,
                           Storage storage,
                           int bufferSize,
                           String dumpPrefix) {
        this.bucketName = bucketName;
        this.storage = storage;
        this.deserializer = deserializer;
        this.bufferSize = bufferSize;
        this.dumpPrefix = dumpPrefix;
    }

    public Optional<String> findLatestStateDump() {
        return findLatestStateDump(all -> true);
    }

    public Optional<String> findLatestStateDump(Predicate<BlobInfo> stateDumpFilter) {
        try {
            Optional<Blob> latestStateDump = getStream(storage.list(bucketName, prefix(dumpPrefix), currentDirectory()))
                    .peek(d -> log.debug("State dump found: {}", d))
                    .filter(stateDumpFilter)
                    .max(comparing(BlobInfo::getName));
            Optional<String> latestStateDumpName = latestStateDump.map(blob -> blob.getName().split("/")[0]);
            log.debug("Latest state dump selected: {}", latestStateDumpName);
            return latestStateDumpName;
        } catch (StorageException e) {
            log.error("Error reading from storage bucket " + bucketName, e);
            throw e;
        }
    }

    private Stream<Blob> getStream(Page<Blob> dumps) {
        return StreamSupport.stream(((Iterable<Blob>) () -> dumps.iterateAll().iterator()).spliterator(), false);
    }

    public Stream<EventType> stream(String stateDumpName) {
        return StreamSupport.stream(new DumpSpliterator(stateDumpName), false);
    }

    @SneakyThrows
    @SuppressFBWarnings("CRLF_INJECTION_LOGS")
    private EventType toEvent(String line) {
        try {
            return deserializer.read(line);
        } catch (IOException e) {
            log.error("Failed to parse event: {}", line);
            throw e;
        }
    }

    private class DumpSpliterator implements Spliterator<EventType> {
        private final Queue<Blob> blobs;
        private BlobSpliterator blobSpliterator;

        public DumpSpliterator(String stateDumpName) {
            blobs = new LinkedList<>(getStream(storage.list(bucketName, prefix(stateDumpName))).toList());
            advanceToNextBlob();
        }

        @Override
        @SneakyThrows
        public boolean tryAdvance(Consumer<? super EventType> action) {
            while (!blobSpliterator.tryAdvance(action)) {
                if (!advanceToNextBlob()) {
                    return false;
                }
            }
            return true;
        }

        private boolean advanceToNextBlob() {
            Blob b = blobs.poll();
            if (b == null) {
                return false;
            }
            blobSpliterator = new BlobSpliterator(b);
            return true;
        }

        @Override
        public Spliterator<EventType> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL;
        }
    }

    private class BlobSpliterator implements Spliterator<EventType> {
        private final BufferedReader reader;

        @SneakyThrows
        public BlobSpliterator(Blob blob) {
            if (blob.getName().endsWith(".gz")) {
                InputStream zippedInputStream = Channels.newInputStream(storage.reader(blob.getBlobId()));
                InputStream unzippedInputStream = new GZIPInputStream(zippedInputStream, bufferSize);
                reader = new BufferedReader(new InputStreamReader(unzippedInputStream, StandardCharsets.UTF_8), bufferSize);
            } else {
                reader = new BufferedReader(Channels.newReader(storage.reader(blob.getBlobId()), StandardCharsets.UTF_8), bufferSize);
            }
        }

        @Override
        @SneakyThrows
        public boolean tryAdvance(Consumer<? super EventType> action) {
            var line = reader.readLine();
            if (line != null) {
                action.accept(toEvent(line));
                return true;
            } else {
                reader.close();
                return false;
            }
        }

        @Override
        public Spliterator<EventType> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL;
        }
    }
}
