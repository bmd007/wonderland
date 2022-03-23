package wonderland.authentication.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.kafka.streams.KafkaStreams.State.REBALANCING;
import static org.apache.kafka.streams.KafkaStreams.State.RUNNING;

@Component
public class KafkaStreamsHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsHealthIndicator.class);
    private static final String MESSAGE_KEY = "Kafka streams";

    private StreamsBuilderFactoryBean streams;
    private AtomicBoolean stillRunning = new AtomicBoolean(false);

    public KafkaStreamsHealthIndicator(StreamsBuilderFactoryBean streams) {
        this.streams = streams;
    }

    public boolean isUp() {
        return streams.isRunning() && stillRunning.get();
    }

    @Override
    public Health health() {
        if (isUp()) {
            return Health.up().withDetail(MESSAGE_KEY, "Available").build();
        }
        return Health.down().withDetail(MESSAGE_KEY, "Not Available").build();
    }

    @PostConstruct
    public void stateAndErrorListener() {
        streams.setUncaughtExceptionHandler((t, e) -> LOGGER.error("uncaught error on kafka streams", e));
        streams.setStateListener((newState, oldState) -> {
            LOGGER.info("transit kafka streams state from {} to {}", oldState, newState);
            stillRunning.set(oldState == REBALANCING && newState == RUNNING);
        });
    }
}