package wonderland.wonder.matcher.streamprocessing;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.StateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import statefull.geofencing.faas.common.domain.Mover;
import statefull.geofencing.faas.common.repository.MoverJdbcRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoverStoreTest {

    private static final String NAME = "store";
    private static final String KEY = "RNO112";
    private static final Mover VALUE = Mover.newBuilder().withId(KEY).build();

    @Mock
    MoverJdbcRepository repository;

    @Mock
    ProcessorContext context;

    @Mock
    StateStore rootStateStore;

    MoverStore store;

    @BeforeEach
    void setUp() throws Exception {
        store = new MoverStore(NAME, repository);
    }

    @Test
    void testName() {
        assertEquals(NAME, store.name());
    }

    @Test
    void testInit() {
        store.init(context, rootStateStore);
        assertTrue(store.isOpen());
        verify(context).register(eq(rootStateStore), any());
    }

    @Test
    void testClose() {
        store.init(null, null);
        store.close();
        assertFalse(store.isOpen());
        verify(repository).deleteAll();
    }

    @Test
    void testPersistent() {
        assertFalse(store.persistent());
    }

    @Test
    void testGetByKey() {
        when(repository.get(KEY)).thenReturn(VALUE);
        assertEquals(VALUE, store.get(KEY));
    }

    @Test
    void testGetByRangeEmpty() {
        assertFalse(store.range(KEY, KEY).hasNext());
    }

    @Test
    void testGetByRange() {
        var key2 = "ZZZ999";
        when(repository.getInRange(KEY, key2)).thenReturn(List.of(VALUE));
        var iterator = store.range(KEY, key2);
        assertTrue(iterator.hasNext());
        var item = iterator.next();
        assertEquals(KEY, item.key);
        assertEquals(VALUE, item.value);
    }

    @Test
    void testGetAll() {
        when(repository.getAll()).thenReturn(List.of(VALUE));
        var iterator = store.all();
        assertTrue(iterator.hasNext());
        var item = iterator.next();
        assertEquals(KEY, item.key);
        assertEquals(VALUE, item.value);
    }

    @Test
    void testNumEntries() {
        when(repository.count()).thenReturn(100L);
        assertEquals(100L, store.approximateNumEntries());
    }

    @Test
    void testPut() {
        store.put(KEY, VALUE);
        verify(repository).save(VALUE);
    }

    @Test
    void testPutAll() {
        store.putAll(List.of(new KeyValue<>(KEY, VALUE)));
        verify(repository).save(VALUE);
    }

    @Test
    void testDelete() {
        store.delete(KEY);
        verify(repository).delete(KEY);
    }
}
