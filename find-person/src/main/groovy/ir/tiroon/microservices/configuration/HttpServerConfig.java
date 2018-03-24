package ir.tiroon.microservices.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.ipc.netty.http.server.HttpServer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class HttpServerConfig {

    @Bean
    HttpServer server(RouterFunction<?> router){
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(router);
        HttpServer httpServer = HttpServer.create(8082);
        httpServer.start(new ReactorHttpHandlerAdapter(httpHandler));
        return httpServer;
    }

    @Bean
    RouterFunction<ServerResponse> monoRouterFunction(CustomerHandler customerHandler) {
        return
                route(GET("/show/person"),customerHandler::showAll)

                ;

    }
}
