import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MeasuredCircuitBreakerRegistry {
    private static final String TARGET_APPLICATION = "target_application";
    private static final String TARGET_TEAM = "target_team";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;

    public MeasuredCircuitBreakerRegistry(MeterRegistry meterRegistry) {
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        this.meterRegistry = meterRegistry;
    }

    public CircuitBreaker getOrRegister(RestCallProperties.CircuitBreakerProperties props) {
        return circuitBreakerRegistry.find(props.targetApplication())
            .orElseGet(() -> createMeasuredCircuitBreaker(props));
    }

    private CircuitBreaker createMeasuredCircuitBreaker(RestCallProperties.CircuitBreakerProperties props) {
        final var circuitBreaker = circuitBreakerRegistry.circuitBreaker(props.targetApplication(),
            getResilience4jConfig(props));

        //Returns 0 or 1 where 1 means OPEN, and 0 means CLOSED
        Gauge.builder("circuit_breaker_status", () -> CircuitBreaker.State.CLOSED.equals(circuitBreaker.getState()) ? 0 : 1)
            .tags(TARGET_TEAM, props.targetTeam(),
                TARGET_APPLICATION, props.targetApplication())
            .register(meterRegistry);
        Gauge.builder("circuit_breaker_failure_rate", () -> circuitBreaker.getMetrics().getFailureRate())
            .tags(TARGET_TEAM, props.targetTeam(), TARGET_APPLICATION, props.targetApplication())
            .register(meterRegistry);
        Gauge.builder("circuit_breaker_failed_calls", () -> circuitBreaker.getMetrics().getNumberOfFailedCalls())
            .tags(TARGET_TEAM, props.targetTeam(), TARGET_APPLICATION, props.targetApplication())
            .register(meterRegistry);

        return circuitBreaker;
    }

    private CircuitBreakerConfig getResilience4jConfig(RestCallProperties.CircuitBreakerProperties props) {
        return CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(props.slidingWindowType()))
            .slidingWindowSize(props.slidingWindowSize())
            .permittedNumberOfCallsInHalfOpenState(props.permittedNumberOfCallsInHalfOpenState())
            .minimumNumberOfCalls(props.minimumNumberOfCalls())
            .waitDurationInOpenState(props.waitDurationInOpenState())
            .build();
    }
}
