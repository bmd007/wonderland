package wonderland.wonder.matcher.streamprocessing;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import statefull.geofencing.faas.common.domain.Mover;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoverProcessorTest {

    private static final String KEY = "RNO112";
    private static final Mover VALUE = Mover.newBuilder().withId(KEY).build();

    @Mock
    ProcessorContext context;

    @Mock
    KeyValueStore<String, Mover> store;

    MoverProcessor processor = new MoverProcessor("store");

    @BeforeEach
    void setUp() throws Exception {
        processor.init(context);
        when(context.getStateStore("store")).thenReturn(store);
    }

    @Test
    void testProcess() {
        processor.process(KEY, VALUE);
        verify(store).put(KEY, VALUE);
    }

}
