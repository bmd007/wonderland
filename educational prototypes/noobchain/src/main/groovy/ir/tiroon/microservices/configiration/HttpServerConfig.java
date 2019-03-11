package ir.tiroon.microservices.configiration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class HttpServerConfig {

//    @Bean
//    RouterFunction<ServerResponse> monoRouterFunction(CustomerHandler customerHandler) {
//        return
//                route(GET("/register/person/{phn}/{name}"), customerHandler::registerPerson)
//                .andRoute(GET("/add/interest/{phn}/{interest}"), customerHandler::addInterest)
//                ;
//    }
}
