package wonderland.authentication;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import wonderland.authentication.config.Stores;
import wonderland.authentication.config.Topics;
import wonderland.authentication.event.external.MessageSentEvent;
import wonderland.authentication.event.internal.CounterIncreasedEvent;
import wonderland.authentication.event.internal.Event;
import wonderland.authentication.event.internal.EventHandler;
import wonderland.authentication.serialization.JsonSerde;

import javax.annotation.PostConstruct;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Logger LOGGER = LoggerFactory.getLogger(KStreamAndKTableDefinitions.class);

    private static final Consumed<String, Event> EVENT_CONSUMED = Consumed.with(Serdes.String(), new JsonSerde(Event.class));
    private static final Produced<String, CounterIncreasedEvent> EVENT_PRODUCED = Produced.with(Serdes.String(), new JsonSerde(CounterIncreasedEvent.class));

    private StreamsBuilder builder;
    private EventHandler eventHandler;

    public KStreamAndKTableDefinitions(StreamsBuilder builder, EventHandler eventHandler) {
        this.builder = builder;
        this.eventHandler = eventHandler;
    }

    @PostConstruct
    public void configureSentMessagesCounterStateStore() {
        builder.stream(Topics.EVENT_LOG, EVENT_CONSUMED)
                .peek((k1, v1) -> LOGGER.debug("Event {} -> {}", k1, v1))
                .groupByKey()
                .aggregate(() -> 0, eventHandler,
                        Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as(Stores.MESSAGE_COUNTER_STATE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new Serdes.IntegerSerde()));
    }
}
