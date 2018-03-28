package ir.tiroon.microservices.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class HttpServerConfig {
    @Bean
    RouterFunction<ServerResponse> routerFunction(CustomerHandler customerHandler) {
        return
                route(GET("/show/users"),customerHandler::showAll)
                        .andRoute(GET("/hello"), customerHandler::hello)
                ;
    }
}
