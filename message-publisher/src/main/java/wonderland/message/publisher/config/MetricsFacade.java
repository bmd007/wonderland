package wonderland.message.publisher.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsFacade {
    private final Counter locationUpdatePublishedCounter;

    public MetricsFacade(MeterRegistry registry) {
        locationUpdatePublishedCounter = registry.counter("wonderland.messages.published.counter");
    }

    public void incrementLocationUpdatePublishedCounter() {
        locationUpdatePublishedCounter.increment();
    }

}
