package wonderland.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/v1/chat/*/queues")
                        .filters(gatewayFilterSpec ->
                                gatewayFilterSpec.rewritePath("/v1/chat/(?<username>\\w+)/queues",
                                        "/create/queue/for/${username}"))
                        .uri("lb://message-publisher")
                )
                .build();
    }
}
