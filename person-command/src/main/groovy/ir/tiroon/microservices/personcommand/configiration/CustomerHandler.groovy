package ir.tiroon.microservices.personcommand.configiration

import ir.tiroon.microservices.personcommand.model.PersonInterestAddedEvent
import ir.tiroon.microservices.personcommand.model.PersonRegisteredEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono;

@Component
class CustomerHandler {

    @Autowired
    KafkaTemplate kafkaTemplate

    Mono<ServerResponse> NotFound = ServerResponse.badRequest().build();
    Mono<ServerResponse> OK = ServerResponse.badRequest().build();


    Mono<ServerResponse> registerPerson(ServerRequest request) {
        def phn = String.valueOf(request.pathVariable("phn"))
        def name = String.valueOf(request.pathVariable("name"))

        kafkaTemplate.send 'mytesttopic', new PersonRegisteredEvent(phn, name)

        OK
    }

    Mono<ServerResponse> addInterest(ServerRequest request) {
        def phn = String.valueOf(request.pathVariable("phn"))
        def interest = String.valueOf(request.pathVariable("interest"))

        kafkaTemplate.send 'mytesttopic', new PersonInterestAddedEvent(phn, interest)

        OK
    }

}
