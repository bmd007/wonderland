package wonderland.message.counter.util;

import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * When using kafka streams stores we need to wait until the right state is ready.<br>
 * EmbeddedKafka doesn't wait for it, so sometimes tests fail.<br>
 * This code cannot be run in a BeforeEach method, it'd be too late as the state listener can only be added during the
 * "CREATE" state...
 * https://stackoverflow.com/questions/53534195/unable-to-open-store-for-kafka-streams-because-invalid-state
 */
public class KafkaStreamsAwait implements StateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsAwait.class);

    private static final Long TIMEOUT = 10_000L;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onChange(State newState, State oldState) {
        LOGGER.info("State changed {}->{}", oldState, newState);
        if (oldState == State.REBALANCING && newState == State.RUNNING) {
            LOGGER.info("Finally state changed REBALANCING->RUNNING");
            latch.countDown();
        }
    }

    public void await() throws InterruptedException {
        latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
    }
}
