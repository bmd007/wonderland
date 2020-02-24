package wonderland.message.counter.event.internal;

import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventHandler implements Aggregator<String, Event, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    @Override
    public Integer apply(String key, Event event, Integer currentValue) {
        if (event instanceof CounterRestartedEvent) {
            return currentValue == null ? 0 : currentValue;
        } else if (event instanceof CounterIncreasedEvent) {
            return currentValue != null ? currentValue + 1 : null; //check to avoid increasing a not already created counter
        }
        LOGGER.error("an event with a not supported yet type received : {} ", event);
        return null; //I think null is just fine here. Saving null in topic should result in anything at the end of the day. Specially
    }
}
