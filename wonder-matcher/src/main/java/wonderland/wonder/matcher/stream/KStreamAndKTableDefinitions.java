package wonderland.wonder.matcher.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.domain.WonderSeekerLikeHistory;
import wonderland.wonder.matcher.domain.WonderSeekerMatchHistory;
import wonderland.wonder.matcher.event.DancePartnerSeekerHasLikedAnotherDancerEvent;
import wonderland.wonder.matcher.event.DancePartnerSeekerIsLikedByAnotherDancerEvent;
import wonderland.wonder.matcher.event.WonderSeekersMatchedEvent;
import wonderland.wonder.matcher.event.DancerIsLookingForPartnerUpdate;
import wonderland.wonder.matcher.repository.WonderSeekerJdbcRepository;

import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.util.Set;

import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_GLOBAL_STATE_STORE;
import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_IN_MEMORY_STATE_STORE;
import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_LIKE_HISTORY_STATE_STORE;
import static wonderland.wonder.matcher.config.StateStores.WONDER_SEEKER_MATCH_HISTORY_STATE_STORE;
import static wonderland.wonder.matcher.config.TopicCreator.stateStoreTopicName;
import static wonderland.wonder.matcher.serialization.CustomSerdes.DANCER_SEEKING_PARTNER_JSON_SERDE;
import static wonderland.wonder.matcher.serialization.CustomSerdes.LIKEES_EVENT_JSON_SERDE;
import static wonderland.wonder.matcher.serialization.CustomSerdes.LIKERS_EVENT_JSON_SERDE;
import static wonderland.wonder.matcher.serialization.CustomSerdes.WONDER_SEEKER_JSON_SERDE;
import static wonderland.wonder.matcher.serialization.CustomSerdes.WONDER_SEEKER_LIKE_HISTORY_JSON_SERDE;
import static wonderland.wonder.matcher.serialization.CustomSerdes.WONDER_SEEKER_MATCH_HISTORY_JSON_SERDE;

@Configuration
public class KStreamAndKTableDefinitions {

    private static final Consumed<String, DancerIsLookingForPartnerUpdate> DANCE_PARTNER_SEEKER_UPDATES_CONSUMED =
            Consumed.with(Serdes.String(), DANCER_SEEKING_PARTNER_JSON_SERDE);
    private static final Consumed<String, DancePartnerSeekerHasLikedAnotherDancerEvent> LIKERS_EVENT_CONSUMED =
            Consumed.with(Serdes.String(), LIKERS_EVENT_JSON_SERDE);
    private static final Produced<String, DancePartnerSeekerIsLikedByAnotherDancerEvent> LIKEES_EVENT_PRODUCED =
            Produced.with(Serdes.String(), LIKEES_EVENT_JSON_SERDE);
    private static final Consumed<String, WonderSeeker> WONDER_SEEKER_CONSUMED = Consumed.with(Serdes.String(), WONDER_SEEKER_JSON_SERDE);

    private static final Materialized<String, WonderSeeker, KeyValueStore<Bytes, byte[]>> WONDER_SEEKER_LOCAL_STATE_KTABLE = Materialized
            .<String, WonderSeeker>as(Stores.inMemoryKeyValueStore(WONDER_SEEKER_IN_MEMORY_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(WONDER_SEEKER_JSON_SERDE);

    private static final Materialized<String, WonderSeekerLikeHistory, KeyValueStore<Bytes, byte[]>> WONDER_SEEKER_LIKE_HISTORY_KTABLE = Materialized
            .<String, WonderSeekerLikeHistory>as(Stores.inMemoryKeyValueStore(WONDER_SEEKER_LIKE_HISTORY_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(WONDER_SEEKER_LIKE_HISTORY_JSON_SERDE);

    private static final Materialized<String, WonderSeekerMatchHistory, KeyValueStore<Bytes, byte[]>> WONDER_SEEKER_MATCH_HISTORY_KTABLE = Materialized
            .<String, WonderSeekerMatchHistory>as(Stores.inMemoryKeyValueStore(WONDER_SEEKER_MATCH_HISTORY_STATE_STORE))
            .withKeySerde(Serdes.String())
            .withValueSerde(WONDER_SEEKER_MATCH_HISTORY_JSON_SERDE);

    private final StreamsBuilder builder;
    private final WonderSeekerJdbcRepository repository;
    private String applicationName;

    public KStreamAndKTableDefinitions(StreamsBuilder builder,
                                       WonderSeekerJdbcRepository repository,
                                       @Value("${spring.application.name}") String applicationName) {
        this.builder = builder;
        this.repository = repository;
        this.applicationName = applicationName;
    }

    @PostConstruct
    public void configureStores() {
        builder.stream(Topics.DANCER_SEEKING_PARTNER_UPDATES, DANCE_PARTNER_SEEKER_UPDATES_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .filter((k, v) -> v.location().latitude() >= -90 && v.location().latitude() <= 90)
                .filter((k, v) -> v.location().longitude() >= -180 && v.location().longitude() <= 180)
                .groupByKey()
                // Aggregate status into an in-memory KTable as a source for global KTable
                // Use an in-memory store for intermediate state storage.
                .aggregate(WonderSeeker::empty,
                        (key, value, aggregate) -> new WonderSeeker(key, value.location(), value.eventTime().toInstant(ZoneOffset.UTC)),
                        WONDER_SEEKER_LOCAL_STATE_KTABLE);

        // register a global store which reads directly from the aggregated in memory table's changelog
        var storeBuilder = new WonderSeekerStore.Builder(WONDER_SEEKER_GLOBAL_STATE_STORE, Time.SYSTEM, repository);
        builder.addGlobalStore(storeBuilder,
                stateStoreTopicName(WONDER_SEEKER_IN_MEMORY_STATE_STORE, applicationName),
                WONDER_SEEKER_CONSUMED,
                () -> new WonderSeekerProcessor(WONDER_SEEKER_GLOBAL_STATE_STORE));

        var wonderSeekerLikeHistoryKTable = builder
                .stream(Topics.DANCE_PARTNER_EVENTS, LIKERS_EVENT_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .groupByKey()
                // Aggregate status into an in-memory KTable as a source for global KTable
                .aggregate(WonderSeekerLikeHistory::empty,
                        (key, value, aggregate) -> {
                            if (aggregate.isEmpty()) {
                                return WonderSeekerLikeHistory.initialize(value.liker()).addLikeToHistory(value.likee(), value.eventTime());
                            }
                            return aggregate.addLikeToHistory(value.likee(), value.eventTime());
                        },
                        WONDER_SEEKER_LIKE_HISTORY_KTABLE);

        builder.stream(Topics.DANCE_PARTNER_EVENTS, LIKERS_EVENT_CONSUMED)
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .map((key, value) -> KeyValue.pair(value.likee(), new DancePartnerSeekerIsLikedByAnotherDancerEvent(value.liker(), value.likee())))
                .repartition()
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .join(wonderSeekerLikeHistoryKTable, (readOnlyKey, passiveFormLikeEvent, wonderSeekerLikeHistory) -> {
                    if (passiveFormLikeEvent.liker().equals(wonderSeekerLikeHistory.wonderSeekerName())) {
                        return wonderSeekerLikeHistory.likeHistory().entrySet().stream()
                                .filter(likeEntry -> likeEntry.getKey().equals(passiveFormLikeEvent.likee()))//todo check time ? time needed at all?
                                .findFirst()//todo needed?
                                .map(likedEntryOfMatch -> new WonderSeekersMatchedEvent(passiveFormLikeEvent.liker(), passiveFormLikeEvent.likee()))
                                .orElseGet(() -> null);
                    }
                    return null;
                })
                .filterNot((k, v) -> k == null || k.isBlank() || k.isEmpty() || v == null || v.key() == null || v.key().isEmpty() || v.key().isBlank())
                .filter((k, v) -> k.equals(v.key()))
                .flatMap((key, value) -> {
                    var reversedKeyMatchEvent = value.reverse();
                    return Set.<KeyValue<String, WonderSeekersMatchedEvent>>of(KeyValue.pair(value.key(), value), KeyValue.pair(reversedKeyMatchEvent.key(), reversedKeyMatchEvent));
                })
//                .repartition(Repartitioned.as())//todo intorduce a new formal topic here so that other people can listen to match events
                .groupByKey()
                .aggregate(WonderSeekerMatchHistory::empty,
                        (key, value, aggregate) -> {
                            if (aggregate.isEmpty()) {
                                return WonderSeekerMatchHistory.initialize(value.matchee1()).addLikeToHistory(value.matchee2(), value.eventTime());
                            }
                            return aggregate.addLikeToHistory(value.matchee2(), value.eventTime());
                        },
                        WONDER_SEEKER_MATCH_HISTORY_KTABLE);
    }
}
