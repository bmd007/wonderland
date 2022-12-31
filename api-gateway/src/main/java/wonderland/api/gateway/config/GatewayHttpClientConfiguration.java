package wonderland.api.gateway.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
public class GatewayHttpClientConfiguration {
    @Bean
    public DiscoveryClientRouteDefinitionLocator discoveryClientRouteLocator(ConsulReactiveDiscoveryClient discoveryClient,
                                                                             DiscoveryLocatorProperties discoveryLocatorProperties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, discoveryLocatorProperties);
    }
}

