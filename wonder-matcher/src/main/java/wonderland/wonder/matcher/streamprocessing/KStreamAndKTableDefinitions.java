package wonderland.wonder.matcher.streamprocessing;

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
import statefull.geofencing.faas.common.domain.Mover;
import statefull.geofencing.faas.common.dto.MoverLocationUpdate;
import statefull.geofencing.faas.common.repository.MoverJdbcRepository;
import wonderland.wonder.matcher.config.MetricsFacade;
import wonderland.wonder.matcher.config.TopicCreator;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import javax.annotation.PostConstruct;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Consumed<String, MoverLocationUpdate> MOVER_POSITION_UPDATE_CONSUMED = Consumed.with(Serdes.String(), CustomSerdes.MOVER_POSITION_UPDATE_JSON_SERDE);
    private static final Consumed<String, Mover> MOVER_CONSUMED = Consumed.with(Serdes.String(), CustomSerdes.MOVER_JSON_SERDE);

    // Use an in-memory store for intermediate state storage.
    private static final Materialized<String, Mover, KeyValueStore<Bytes, byte[]>> IN_MEMORY_TEMP_KTABLE = Materialized
            .<String, Mover>as(Stores.inMemoryKeyValueStore(wonderland.wonder.matcher.config.Stores.MOVER_IN_MEMORY_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(CustomSerdes.MOVER_JSON_SERDE);

    private static final Logger LOGGER = LoggerFactory.getLogger(KStreamAndKTableDefinitions.class);

    private final Predicate<MoverLocationUpdate> moverLocationUpdateFilterFunction;
    private final BiFunction<Mover, MoverLocationUpdate, Mover> moverAggregateFunction;
    private final StreamsBuilder builder;
    private final MoverJdbcRepository repository;
    private final String applicationName;
    private final MetricsFacade metrics;

    public KStreamAndKTableDefinitions(Predicate<MoverLocationUpdate> moverLocationUpdateFilterFunction,
                                       BiFunction<Mover, MoverLocationUpdate, Mover> moverAggregateFunction,
                                       StreamsBuilder builder, MoverJdbcRepository repository,
                                       @Value("${spring.application.name}") String applicationName, MetricsFacade metrics) {
        this.moverLocationUpdateFilterFunction = moverLocationUpdateFilterFunction;
        this.moverAggregateFunction = moverAggregateFunction;
        this.builder = builder;
        this.repository = repository;
        this.applicationName = applicationName;
        this.metrics = metrics;
    }

    @PostConstruct
    public void configureStores() {
        builder
                .stream(Topics.MOVER_POSITION_UPDATES_TOPIC, MOVER_POSITION_UPDATE_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.getKey() == null || v.getKey().isEmpty() || v.getKey().isBlank())
                .filter((k, v) -> k.equals(v.getKey()))
                .filter((k, v) -> v.getLatitude() >= -90 && v.getLatitude() <= 90)
                .filter((k, v) -> v.getLongitude() >= -180 && v.getLongitude() <= 180)
                .filter((key, value) -> moverLocationUpdateFilterFunction.test(value))
                .groupByKey()
                // Aggregate status into a in-memory KTable as a source for global KTable
                .aggregate(Mover::defineEmpty,
                        (key, value, aggregate) -> moverAggregateFunction
                                .andThen(mover -> {
                                    metrics.incrementAggregationCounter();
                                    return mover;
                                })
                                .apply(aggregate, value),
                        IN_MEMORY_TEMP_KTABLE);

        // register a global store which reads directly from the aggregated in memory table's changelog
        var storeBuilder = new MoverStore.Builder(wonderland.wonder.matcher.config.Stores.MOVER_GLOBAL_STATE_STORE, Time.SYSTEM, repository);
        builder.addGlobalStore(storeBuilder,
                TopicCreator.storeTopicName(wonderland.wonder.matcher.config.Stores.MOVER_IN_MEMORY_STATE_STORE, applicationName),
                MOVER_CONSUMED, () -> new MoverProcessor(wonderland.wonder.matcher.config.Stores.MOVER_GLOBAL_STATE_STORE));
    }

}
