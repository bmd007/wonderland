package wonderland.wonder.matcher.stream;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wonderland.wonder.matcher.domain.WonderSeeker;

import static java.util.Objects.requireNonNull;

/**
 * A simple processor that stores the state in a key value store.
 */
public class WonderSeekerProcessor extends ContextualProcessor<String, WonderSeeker, Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WonderSeekerProcessor.class);

    private final String storeName;

    public WonderSeekerProcessor(String storeName) {
        this.storeName = storeName;
    }


    //todo understand how this context works??
    @SuppressWarnings("unchecked")
    private KeyValueStore<String, WonderSeeker> getStore() {
        var store = requireNonNull(this.context().getStateStore(storeName), "Store not found");
        if (store instanceof KeyValueStore) {
            return (KeyValueStore<String, WonderSeeker>) store;
        }
        throw new IllegalStateException("Not a key value store");
    }

    @Override
    public void process(Record<String, WonderSeeker> kafkaRecord) {
        LOGGER.info("Processing {}, (saving it in global store)", kafkaRecord);
        getStore().put(kafkaRecord.key(), kafkaRecord.value());
    }

}
