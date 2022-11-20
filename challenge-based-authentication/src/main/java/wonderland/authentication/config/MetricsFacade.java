package wonderland.authentication.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;


@Component
public class MetricsFacade {

    //todo use the metrics in correct places

    public static void increaseHandledEventsCounter(MeterRegistry registry) {
        registry.counter("wonderland.authentication.events.handled.counter").increment();
    }

    public static void increaseProducedEventsCounter(MeterRegistry registry) {
        registry.counter("wonderland.authentication.events.produced.counter").increment();
    }

    public static void increaseSuccessfulAuthenticationsCounter(MeterRegistry registry) {
        registry.counter("wonderland.authentication.successful.counter").increment();
    }

    public static void increaseFailedAuthenticationsCounter(MeterRegistry registry) {
        registry.counter("wonderland.authentication.failed.counter").increment();
    }

}
