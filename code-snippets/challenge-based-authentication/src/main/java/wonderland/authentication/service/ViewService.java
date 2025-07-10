package wonderland.authentication.service;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wonderland.authentication.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

/**
 * Each view_resource class has 3 related data classes:<br>
 * <li>I is internal representation of view which is the equivalent to the schema by which data is saved on stores.</li>
 * <li>M is the external representation of I (dto)</li>
 * <li>E is a Dto that includes a list of Ms. So for getting more than one instance of I, instead of</li>
 * <code>Flux&lt;M></code>, it will be <code>Mono&lt;E></code> <br>
 * For now we don't test this class separately. Instead, we test each resource class that uses this class.
 **/
public class ViewService<E, M, I> {

    public static final String HIGH_LEVEL_QUERY_PARAM_NAME = "isHighLevelQuery";
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewService.class);
    private final String storeName;
    private final Class<E> externalClass;
    private final Class<M> middleClass;
    private final BiFunction<String, I, M> mapperFromItoE;
    private final Function<E, List<M>> listOfMfromEextractor;
    private final Function<List<M>, E> listOfMtoE;
    private final String pathPart;
    private String ip;
    private int port;
    private StreamsBuilderFactoryBean streams;
    private ViewResourcesClient commonClient;

    public ViewService(String ip, int port,
                       StreamsBuilderFactoryBean streams,
                       String storeName,
                       Class<E> externalClass, Class<M> middleClass,
                       BiFunction<String, I, M> mapperFromItoE,
                       Function<E, List<M>> listOfMfromEextractor,
                       Function<List<M>, E> listOfMtoE,
                       String pathPart,
                       ViewResourcesClient commonClient) {
        this.ip = ip;
        this.port = port;
        this.streams = streams;
        this.storeName = storeName;
        this.externalClass = externalClass;
        this.middleClass = middleClass;
        this.mapperFromItoE = mapperFromItoE;
        this.listOfMfromEextractor = listOfMfromEextractor;
        this.listOfMtoE = listOfMtoE;
        this.pathPart = pathPart;
        this.commonClient = commonClient;
    }

    public Mono<E> getAll(boolean isHighLevelQuery) {
        var localData = getFromLocalStorage();
        if (isHighLevelQuery) {
            var remoteData = getAllFromRemoteStorage();
            var allData = Flux.concat(localData, remoteData);
            return allData.collectList().map(listOfMtoE);
        } else {
            return localData.collectList().map(listOfMtoE);
        }
    }

    //**
    // Use this method for fetching all of the data ONLY if the assigned store is GLOBAL
    // **//
    public Mono<E> getAllFromGlobalStore() {
        return getFromLocalStorage().collectList().map(listOfMtoE);
    }

    private Flux<M> getAllFromRemoteStorage() {
        var metadataCollection = Objects.requireNonNull(streams.getKafkaStreams()).streamsMetadataForStore(storeName);
        return Flux.fromIterable(metadataCollection)
                .switchIfEmpty(Flux.error(() -> new ServiceUnavailableException("No metadata found for " + storeName)))
                .filter(this::isRemoteNode)
                .flatMap(this::getFromRemoteStorage)
                .onErrorResume(throwable -> Flux.error(() -> new ServiceUnavailableException("Error " + throwable.getMessage() + " when getting all for store" + storeName)));
    }

    private Flux<M> getFromRemoteStorage(StreamsMetadata metadata) {
        String url = String.format("http://%s:%d/%s?%s=false",
                metadata.host(),
                metadata.port(),
                pathPart,
                HIGH_LEVEL_QUERY_PARAM_NAME);
        return commonClient.getOne(externalClass, url).flatMapIterable(listOfMfromEextractor);
    }

    private Flux<M> getFromLocalStorage() {
        var store = waitUntilStoreIsQueryable();
        var it = store.all(); // hold a reference to close it later
        return Flux.fromIterable(() -> it)
                .filter(kv -> !isNullOrEmpty(kv.key))
                .filter(kv -> !isNull(kv.value))
                .map(kv -> mapperFromItoE.apply(kv.key, kv.value))
                .doAfterTerminate(it::close);
    }

    private boolean isRemoteNode(StreamsMetadata metadata) {
        return !metadata.host().equals(ip) || metadata.port() != port;
    }

    public Mono<M> getById(String key) {
        var metadata = streams.getKafkaStreams().queryMetadataForKey(storeName, key, new StringSerializer());

        if (metadata.equals(KeyQueryMetadata.NOT_AVAILABLE) || metadata == null) {
            LOGGER.error("Neither this nor other instances has access to requested key. Metadata: {}", metadata);
            return Mono.empty();//No metadata for that key
        }

        if (metadata.activeHost().host().equals(ip) && metadata.activeHost().port() == port) {
            LOGGER.debug("Querying local store {} for key: {}", storeName, key);
            var store = waitUntilStoreIsQueryable();
            return Optional.ofNullable(store.get(key))
                    .map(i -> mapperFromItoE.apply(key, i))
                    .map(Mono::just)
                    .orElseGet(() -> Mono.empty());//No data for that key locally
        }

        var url = String.format("http://%s:%d/%s/%s",
                metadata.activeHost().host(),
                metadata.activeHost().port(),
                pathPart,
                key);
        LOGGER.debug("Querying other instance's {} store for key: {} from {}", storeName, key, url);
        return commonClient.getOne(middleClass, url)
                .switchIfEmpty(Mono.empty());//No data for that key remotely
    }

    public Mono<M> getByIdFromGlobalStore(String id) {
        var metadata = streams.getKafkaStreams().queryMetadataForKey(storeName, id, new StringSerializer());

        if (metadata.equals(KeyQueryMetadata.NOT_AVAILABLE) || metadata == null) {
            LOGGER.error("Neither this nor other instances has access to requested key. Metadata: {}", metadata);
            return Mono.empty();//No metadata for that key
        }

        LOGGER.debug("Querying local part of global store {} for id: {}", storeName, id);
        var store = waitUntilStoreIsQueryable();
        return Optional.ofNullable(store.get(id))
                .map(i -> mapperFromItoE.apply(id, i))
                .map(Mono::just)
                .orElseGet(() -> Mono.empty());//No data for that key locally
    }

    public ReadOnlyKeyValueStore<String, I> waitUntilStoreIsQueryable() {
        for (int i = 0; i < 10; i++) {
            try {
                var storeQueryParameters = StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.<String, I>keyValueStore());
                return streams.getKafkaStreams().store(storeQueryParameters);
            } catch (InvalidStateStoreException e2) {
                // store not yet ready for querying
                LOGGER.error("Invalid State Store Error while fetching the kafkaStream store: {}. A retry will happen after 300 ms", storeName);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    LOGGER.error("interrupted while waiting for streams store to be ready");
                    throw new ServiceUnavailableException("interrupted while waiting for streams store to be ready");
                }
            }
        }
        throw new ServiceUnavailableException("Invalid State Store Error while fetching the kafkaStream store. The service is not available at the moment");
    }
}
