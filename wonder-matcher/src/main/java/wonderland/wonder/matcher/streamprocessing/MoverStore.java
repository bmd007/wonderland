package wonderland.wonder.matcher.streamprocessing;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.internals.AbstractStoreBuilder;
import org.apache.kafka.streams.state.internals.DelegatingPeekingKeyValueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statefull.geofencing.faas.common.domain.Mover;
import statefull.geofencing.faas.common.repository.MoverJdbcRepository;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoverStore implements KeyValueStore<String, Mover> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoverStore.class);

    private final String name;
    private final MoverJdbcRepository repository;
    private boolean open = false;

    public MoverStore(String name, MoverJdbcRepository repository) {
        this.name = name;
        this.repository = repository;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void init(ProcessorContext context, StateStore root) {
        LOGGER.info("Initializing Mover Global State Store");
        if (root != null) {
            // register the store (copied from InMemoryKeyValueStore)
            context.register(root, (keyBytes, valueBytes) -> {
                var key = Serdes.String().deserializer().deserialize(null, valueBytes);
                // this is a deletion operation
                if (valueBytes == null) {
                    delete(key);
                } else {
                    Mover value = CustomSerdes.MOVER_JSON_SERDE.deserializer().deserialize(null, valueBytes);
                    put(key, value);
                }
            });
        }
        open = true;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        LOGGER.info("Closing Mover Global State Store");
        try {
            repository.deleteAll();
        } catch (Exception e) {
            LOGGER.warn("Exception when trying to close the store", e);
        }
        open = false;
    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public Mover get(String key) {
        LOGGER.debug("Reading by key {}", key);
        return repository.get(key);
    }

    @Override
    public KeyValueIterator<String, Mover> range(String from, String to) {
        LOGGER.debug("Reading rage {}-{}", from, to);
        if (from.compareTo(to) > 0) {
            return new EmptyKeyValueIterator<>();
        }
        return wrapWithIterator(repository.getInRange(from, to));
    }

    @Override
    public KeyValueIterator<String, Mover> all() {
        LOGGER.debug("Reading all movers");
        return wrapWithIterator(repository.getAll());
    }

    @Override
    public long approximateNumEntries() {
        return repository.count();
    }

    @Override
    public void put(String key, Mover value) {
        LOGGER.debug("Updating {}", key);
        if (key == null) {
            return;
        }
        if (value == null) {
            repository.delete(key);
        } else if (value.getId() == null || value.getId().isBlank() || value.getId().isEmpty()) {
            return;
        } else {
            errorSuppressedSave(value);
        }
    }

    @Override
    public Mover putIfAbsent(String key, Mover value) {
        LOGGER.info("Updating {} if absent", key);
        var original = get(key);
        if (original == null) {
            errorSuppressedSave(value);
        }
        return original;
    }

    private void errorSuppressedSave(Mover value) {
       try {
           repository.save(value);
       }catch (Exception e){
           LOGGER.error("Problem while saving {}", value, e);
       }
    }

    @Override
    public void putAll(List<KeyValue<String, Mover>> entries) {
        LOGGER.debug("Updating multiple ({}) entries", entries.size());
        for (var entry : entries) {
            put(entry.key, entry.value);
        }
    }

    @Override
    public Mover delete(String key) {
        LOGGER.debug("Removing {}", key);
        var original = repository.get(key);
        repository.delete(key);
        return original;
    }

    private KeyValueIterator<String, Mover> wrapWithIterator(List<Mover> all) {
        var map = all.stream().collect(Collectors.toMap(Mover::getId, Function.identity()));
        return new DelegatingPeekingKeyValueIterator<>(name, new InMemoryKeyValueIterator<>(map.entrySet().iterator()));
    }

    public static class Builder extends AbstractStoreBuilder<String, Mover, MoverStore> {

        private final MoverJdbcRepository repository;

        public Builder(String name, Time time, MoverJdbcRepository repository) {
            super(name, Serdes.String(), CustomSerdes.MOVER_JSON_SERDE, time);
            this.repository = repository;
        }

        @Override
        public MoverStore build() {
            return new MoverStore(name, repository);
        }
    }

    private static class InMemoryKeyValueIterator<K, V> implements KeyValueIterator<K, V> {

        private final Iterator<Map.Entry<K, V>> iter;

        private InMemoryKeyValueIterator(final Iterator<Map.Entry<K, V>> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public KeyValue<K, V> next() {
            Map.Entry<K, V> entry = iter.next();
            return new KeyValue<>(entry.getKey(), entry.getValue());
        }

        @Override
        public void remove() {
            iter.remove();
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public K peekNextKey() {
            throw new UnsupportedOperationException("peekNextKey() not supported in " + getClass().getName());
        }
    }

    private static class EmptyKeyValueIterator<K, V> implements KeyValueIterator<K, V> {

        @Override
        public void close() {
        }

        @Override
        public K peekNextKey() {
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public KeyValue<K, V> next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
        }
    }
}
