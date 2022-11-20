package wonderland.authentication;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import wonderland.authentication.config.MetricsFacade;
import wonderland.authentication.config.Stores;
import wonderland.authentication.config.Topics;
import wonderland.authentication.domain.AuthenticationChallenge;
import wonderland.authentication.event.internal.Event;
import wonderland.authentication.event.internal.EventHandler;
import wonderland.authentication.serialization.JsonSerde;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(KStreamAndKTableDefinitions.class);

    private static final Consumed<String, Event> EVENT_CONSUMED = Consumed.with(Serdes.String(), new JsonSerde(Event.class));
//    private static final Produced<String, Event> EVENT_PRODUCED = Produced.with(Serdes.String(), new JsonSerde(Event.class));

    private StreamsBuilder builder;
    private EventHandler eventHandler;
    private MeterRegistry meterRegistry;

    public KStreamAndKTableDefinitions(StreamsBuilder builder, EventHandler eventHandler, MeterRegistry meterRegistry) {
        this.builder = builder;
        this.eventHandler = eventHandler;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void configureSentMessagesCounterStateStore() {
        builder.stream(Topics.EVENT_LOG, EVENT_CONSUMED)
                .peek((key, value) -> LOGGER.debug("Event {} -> {}", key, value))
                .peek((key, value) -> MetricsFacade.increaseHandledEventsCounter(meterRegistry))
                .groupByKey()
                .aggregate(AuthenticationChallenge::createNew,
                        eventHandler,
                        Materialized.<String, AuthenticationChallenge, KeyValueStore<Bytes, byte[]>>as(Stores.CHALLENGE_STATE_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(AuthenticationChallenge.class)));
    }
}
