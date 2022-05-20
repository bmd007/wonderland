package wonderland.wonder.matcher.streamprocessing;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wonderland.wonder.matcher.domain.WonderSeeker;

import org.apache.kafka.streams.processor.api.Record;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WonderSeekerProcessorTest {

    private static final String KEY = "RNO112";
    private static final WonderSeeker VALUE = WonderSeeker.empty(KEY);

    @Mock
    ProcessorContext context;

    @Mock
    KeyValueStore<String, WonderSeeker> store;

    WonderSeekerProcessor processor = new WonderSeekerProcessor("store");

    @BeforeEach
    void setUp() throws Exception {
        when(context.getStateStore("store")).thenReturn(store);
    }

    @Test
    void testProcess() {
        processor.process(new Record<String, WonderSeeker>(KEY, VALUE, Instant.now().toEpochMilli()));
        verify(store).put(KEY, VALUE);
    }

}
