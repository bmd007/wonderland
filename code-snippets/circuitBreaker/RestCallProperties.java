package se..companydata.keyfigures.config;

import lombok.NonNull;

import java.time.Duration;

public record RestCallProperties(@NonNull Integer timeout,
                                 CircuitBreakerProperties circuitbreaker) {

    // represents a subset of fields in io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
    public record CircuitBreakerProperties(
        @NonNull String targetApplication,
        @NonNull String targetTeam,
        String slidingWindowType,
        Integer slidingWindowSize,
        Integer permittedNumberOfCallsInHalfOpenState,
        Integer minimumNumberOfCalls,
        Duration waitDurationInOpenState
    ) {
        public CircuitBreakerProperties {
            if (slidingWindowType == null) slidingWindowType = "TIME_BASED";
            if (slidingWindowSize == null) slidingWindowSize = 10;
            if (permittedNumberOfCallsInHalfOpenState == null) permittedNumberOfCallsInHalfOpenState = 10;
            if (minimumNumberOfCalls == null) minimumNumberOfCalls = 10;
            if (waitDurationInOpenState == null) waitDurationInOpenState = Duration.ofSeconds(10);
        }
    }
}
