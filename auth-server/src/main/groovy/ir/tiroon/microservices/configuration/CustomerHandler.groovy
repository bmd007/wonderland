package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.service.UserServices
import ir.tiroon.microservices.model.userManagement.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CustomerHandler {

    @Autowired
    UserServices userServices

    Mono<ServerResponse> showAll(ServerRequest request) {
        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.fromStream(userServices.getList().stream()),User.class)
    }


    Mono<ServerResponse> hello(ServerRequest serverRequest) {
        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(serverRequest.principal().block().name))
    }

}
