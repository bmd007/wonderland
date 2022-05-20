package wonderland.wonder.matcher.streamprocessing;

import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statefull.geofencing.faas.common.domain.Mover;

import static java.util.Objects.requireNonNull;

/**
 * A simple processor that stores the state in a key value store.
 */
public class WonderSeekerProcessor extends AbstractProcessor<String, Mover> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WonderSeekerProcessor.class);

    private final String storeName;

    public WonderSeekerProcessor(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void process(String key, Mover value) {
        LOGGER.info("Processing {}", key);
        getStore().put(key, value);
    }

    //todo understand how this context works??
    @SuppressWarnings("unchecked")
    private KeyValueStore<String, Mover> getStore() {
        var store = requireNonNull(this.context().getStateStore(storeName), "Store not found");
        if (store instanceof KeyValueStore) {
            return (KeyValueStore<String, Mover>) store;
        }
        throw new IllegalStateException("Not a key value store");
    }

}
