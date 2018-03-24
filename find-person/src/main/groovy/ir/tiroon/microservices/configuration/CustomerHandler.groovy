package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.model.PersonRegisteredEvent
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CustomerHandler {

//    @Autowired
//    PersonRegisteredEventRepository prer


    Mono<ServerResponse> showAll(ServerRequest request) {
        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(findAll(), PersonRegisteredEvent.class)
    }

    Flux<PersonRegisteredEvent> findAll(){
        def pre = new PersonRegisteredEvent("phone","123123")
        Flux.fromArray([pre,pre])
    }

}
