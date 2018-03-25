package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.model.Person
import ir.tiroon.microservices.model.PersonRegisteredEvent
import ir.tiroon.microservices.repository.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CustomerHandler {

    @Autowired
    PersonRepository personRepo;


    Mono<ServerResponse> showAll(ServerRequest request) {
        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.fromStream(personRepo.findAll().stream()),Person.class)
    }


}
