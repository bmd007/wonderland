package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.model.PersonInterest
import ir.tiroon.microservices.repository.PersonInterestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CustomerHandler {

    @Autowired
    PersonInterestRepository personInterestRepo

    Mono<ServerResponse> showInterests(ServerRequest request) {

        def personInterest = personInterestRepo.findByPhoneNumber(String.valueOf(request.pathVariable("phn")))

        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(personInterest,PersonInterest.class)
    }


}
