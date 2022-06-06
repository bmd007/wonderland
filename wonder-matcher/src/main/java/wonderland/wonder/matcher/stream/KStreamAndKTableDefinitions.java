package wonderland.wonder.matcher.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Configuration;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.dto.DancerIsLookingForPartnerUpdate;
import wonderland.wonder.matcher.repository.WonderSeekerJdbcRepository;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import javax.annotation.PostConstruct;

import java.time.ZoneOffset;

import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_GLOBAL_STATE_STORE;
import static wonderland.wonder.matcher.config.TopicCreator.stateStoreTopic;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Consumed<String, DancerIsLookingForPartnerUpdate> DANCE_PARTNER_SEEKER_UPDATES_CONSUMED =
            Consumed.with(Serdes.String(), CustomSerdes.DANCER_SEEKING_PARTNER_JSON_SERDE);
    private static final Consumed<String, WonderSeeker> WONDER_SEEKER_CONSUMED = Consumed.with(Serdes.String(), CustomSerdes.WONDER_SEEKER_JSON_SERDE);

    // Use an in-memory store for intermediate state storage.
    private static final Materialized<String, WonderSeeker, KeyValueStore<Bytes, byte[]>> IN_MEMORY_TEMP_KTABLE = Materialized
            .<String, WonderSeeker>as(Stores.inMemoryKeyValueStore(StateStores.WONDER_SEEKER_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(CustomSerdes.WONDER_SEEKER_JSON_SERDE);

    private final StreamsBuilder builder;
    private final WonderSeekerJdbcRepository repository;

    public KStreamAndKTableDefinitions(StreamsBuilder builder, WonderSeekerJdbcRepository repository) {
        this.builder = builder;
        this.repository = repository;
    }

    @PostConstruct
    public void configureStores() {
        builder
                .stream(Topics.DANCER_SEEKING_PARTNER_UPDATES, DANCE_PARTNER_SEEKER_UPDATES_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .filter((k, v) -> v.location().latitude() >= -90 && v.location().latitude() <= 90)
                .filter((k, v) -> v.location().longitude() >= -180 && v.location().longitude() <= 180)
                .groupByKey()
                // Aggregate status into an in-memory KTable as a source for global KTable
                .aggregate(WonderSeeker::empty,
                        (key, value, aggregate) -> new WonderSeeker(key, value.location(), value.eventTime().toInstant(ZoneOffset.UTC)),
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
