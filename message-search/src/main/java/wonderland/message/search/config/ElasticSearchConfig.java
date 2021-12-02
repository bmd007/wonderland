package wonderland.message.search.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.util.Arrays;

@Configuration
@EnableReactiveElasticsearchRepositories
public class ElasticSearchConfig extends AbstractReactiveElasticsearchConfiguration {


    private final DiscoveryClient discoveryClient;
    private final Environment environment;

    public ElasticSearchConfig(DiscoveryClient discoveryClient, Environment environment) {
        this.discoveryClient = discoveryClient;
        this.environment = environment;
    }

    @Bean
    @Override
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        var exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        var elasticSearchHost = "localhost";

        boolean isLocalProfiileActive = Arrays.stream(environment.getActiveProfiles())
                .filter(profile -> profile.equals("local"))
                .count() != 0;

        if (!isLocalProfiileActive) {
            elasticSearchHost = discoveryClient.getInstances("elasticsearch")
                    .stream()
                    .map(ServiceInstance::getHost)
                    .findFirst()
                    .map(host -> host.contains("http://") ?  host.split("http://")[1] : host)
                    .orElseThrow();
        }
        elasticSearchHost += ":9200";

        var clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticSearchHost)
                .withWebClientConfigurer(webClient -> webClient.mutate().exchangeStrategies(exchangeStrategies).build())
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }
}
