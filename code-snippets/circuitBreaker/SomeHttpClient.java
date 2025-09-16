
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import se..companydata.keyfigures.config.MeasuredCircuitBreakerRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class InstrumentDomainClient {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;
    private final ExecutorService virtualThreadExecutor;
    private final MeasuredCircuitBreakerRegistry circuitBreakerRegistry;
    private final InstrumentDomainProperties instrumentDomainProperties;

    public CompletableFuture<ShortSales> getShortSales(String orderBookId, Locale locale) {
        var uri = UriComponentsBuilder.fromUri(baseUrl)
            .path("/v1/shortsales")
            .pathSegment(orderBookId)
            .build()
            .toUri();

        Supplier<ShortSales> httpCall = () ->
            restClient.get()
                .uri(uri)
                .header("x-locele", locale.name())
                .retrieve()
                .body(ShortSales.class);

        Supplier<ShortSales> measuredHttpCall =
            Timer.builder("rest_calls.rtt")
                .tag("scenario", "get_short_sales")
                .publishPercentileHistogram()
                .register(meterRegistry)
                .wrap(httpCall);

        return circuitBreakerRegistry.getOrRegister(instrumentDomainProperties.shortSales().circuitbreaker())
            .executeCompletionStage(() ->
                CompletableFuture.supplyAsync(measuredHttpCall, virtualThreadExecutor)
                    .orTimeout(instrumentDomainProperties.shortSales().timeout(), TimeUnit.MILLISECONDS)
            )
            .exceptionally(exception -> {
                log.atWarn()
                    .setMessage("Error when fetching short sales")
                    .addKeyValue("orderBookId", orderBookId)
                    .setCause(exception)
                    .log();
                return null;
            })
            .toCompletableFuture();
    }
}
