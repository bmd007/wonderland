package wonderland.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@EnableDiscoveryClient
@SpringBootApplication
public class BankApiGatewayApplication {

    static void main(String[] args) {
        SpringApplication.run(BankApiGatewayApplication.class, args);
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.addAllowedHeader("*");
//        corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost:[0-9]+"));
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/v1/chat/queues/user/*")
                        .filters(gatewayFilterSpec ->
                                gatewayFilterSpec.rewritePath("/v1/chat/queues/user/(?<username>\\w+)",
                                        "/bindings/user/${username}")
                        )
                        .uri("lb://message-publisher")
                )
                .route(r -> r.path("/v1/chat/messages").uri("lb://message-publisher"))
                .route(r -> r.path("/v1/chat/video/signaling").uri("lb://message-publisher"))
                .build();
    }

    @Bean
    public DiscoveryClientRouteDefinitionLocator discoveryClientRouteLocator(ConsulReactiveDiscoveryClient discoveryClient,
                                                                             DiscoveryLocatorProperties discoveryLocatorProperties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, discoveryLocatorProperties);
    }
}
