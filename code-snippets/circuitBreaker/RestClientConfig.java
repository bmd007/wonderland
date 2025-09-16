package se..companydata.keyfigures.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.ACCEPT;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient intraRestClient(RestClient.Builder restClientBuilder,
                                             ClientHttpRequestFactoryBuilder<ClientHttpRequestFactory> clientHttpRequestFactoryBuilder) {
        return restClientBuilder
            .defaultHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .requestFactory(clientHttpRequestFactoryBuilder
                .build(ClientHttpRequestFactorySettings.defaults()
                    .withConnectTimeout(Duration.ofMillis(1000))
                    .withReadTimeout(Duration.ofMillis(1000))))
            .build();
    }
}
