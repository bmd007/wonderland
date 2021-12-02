package wonderland.message.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

@Configuration
@EnableReactiveElasticsearchRepositories
public class ElasticSearchConfig extends AbstractReactiveElasticsearchConfiguration {

    @Value("${elasticsearch.rest.ip}")
    private String elasticSearchRestIp;

    @Bean
    @Override
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        var exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();
        var clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticSearchRestIp+":9200")
                .withWebClientConfigurer(webClient -> webClient.mutate().exchangeStrategies(exchangeStrategies).build())
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }
}
