package wonderland.wonder.matcher.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.dto.SeekerWonderingUpdateDto;
import wonderland.wonder.matcher.repository.WonderSeekerJdbcRepository;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import javax.annotation.PostConstruct;

import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_GLOBAL_STATE_STORE;
import static wonderland.wonder.matcher.config.TopicCreator.stateStoreTopic;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Consumed<String, SeekerWonderingUpdateDto> SEEKER_WONDERING_UPDATES_CONSUMED = Consumed.with(Serdes.String(), CustomSerdes.WONDER_SEEKER_DTO_JSON_SERDE);
    private static final Consumed<String, WonderSeeker> WONDER_SEEKER_CONSUMED = Consumed.with(Serdes.String(), CustomSerdes.WONDER_SEEKER_JSON_SERDE);

    // Use an in-memory store for intermediate state storage.
    private static final Materialized<String, WonderSeeker, KeyValueStore<Bytes, byte[]>> IN_MEMORY_TEMP_KTABLE = Materialized
            .<String, WonderSeeker>as(Stores.inMemoryKeyValueStore(StateStores.WONDER_SEEKER_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(CustomSerdes.WONDER_SEEKER_JSON_SERDE);

    private static final Logger LOGGER = LoggerFactory.getLogger(KStreamAndKTableDefinitions.class);

    private final StreamsBuilder builder;
    private final WonderSeekerJdbcRepository repository;
    private final String applicationName;

    public KStreamAndKTableDefinitions(StreamsBuilder builder,
                                       WonderSeekerJdbcRepository repository,
                                       @Value("${spring.application.name}") String applicationName) {
        this.builder = builder;
        this.repository = repository;
        this.applicationName = applicationName;
    }

    @PostConstruct
    public void configureStores() {
        builder
                .stream(Topics.WONDER_SEEK_UPDATES_TOPIC, SEEKER_WONDERING_UPDATES_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.getKey() == null || v.getKey().isEmpty() || v.getKey().isBlank())
                .filter((k, v) -> k.equals(v.getKey()))
                .filter((k, v) -> v.latitude() >= -90 && v.latitude() <= 90)
                .filter((k, v) -> v.longitude() >= -180 && v.longitude() <= 180)
                .groupByKey()
                // Aggregate status into an in-memory KTable as a source for global KTable
                .aggregate(WonderSeeker::defineEmpty,
                        (key, value, aggregate) -> {
                        },
                        IN_MEMORY_TEMP_KTABLE);

        // register a global store which reads directly from the aggregated in memory table's changelog
        var storeBuilder = new WonderSeekerStore.Builder(WONDER_SEEKER_GLOBAL_STATE_STORE, Time.SYSTEM, repository);
        builder.addGlobalStore(storeBuilder,
                stateStoreTopic(WONDER_SEEKER_GLOBAL_STATE_STORE),
                WONDER_SEEKER_CONSUMED,
                () -> new WonderSeekerProcessor(WONDER_SEEKER_GLOBAL_STATE_STORE)
        );
    }

}
