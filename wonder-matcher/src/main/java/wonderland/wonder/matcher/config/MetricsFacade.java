package wonderland.wonder.matcher.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;


@Component
public class MetricsFacade {

    private final Counter moverAggregationCounter;
    private final Counter queryByFenceCounter;

    public MetricsFacade(MeterRegistry registry) {
        moverAggregationCounter = registry.counter("geofencing.mover.aggregation.counter");
        queryByFenceCounter = registry.counter("geofencing.mover.query.by.fence.counter");
    }

    public void incrementAggregationCounter() {
        moverAggregationCounter.increment();
    }

    public void incrementQueryByFenceCounter() {
        queryByFenceCounter.increment();
    }
}
