package ir.tiroon.microservices.personcommand.configiration


import ir.tiroon.microservices.personcommand.model.PersonInterestAddedEvent
import ir.tiroon.microservices.personcommand.model.PersonRegisteredEvent
import ir.tiroon.microservices.personcommand.repository.InterestAddedEventRepository
import ir.tiroon.microservices.personcommand.repository.PersonRegisteredEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CustomerHandler {

    @Autowired
    KafkaTemplate kafkaTemplate

    @Autowired
    PersonRegisteredEventRepository prer

    @Autowired
    InterestAddedEventRepository iaer

    Mono<ServerResponse> registerPerson(ServerRequest request) {
        def phn = String.valueOf(request.pathVariable("phn"))
        def name = String.valueOf(request.pathVariable("name"))

        def event = new PersonRegisteredEvent(phn, name)

        //this is fully reactive
        //prer.save(event).subscribe()

        //but beacuse we need to send event only after save completion,
        //we should use block()
        def savedEvent = prer.save(event).block()

        kafkaTemplate.send('mytesttopic', event)

        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).
                body(Mono.just(savedEvent), PersonRegisteredEvent.class)
    }

    Mono<ServerResponse> addInterest(ServerRequest request) {
        def phn = String.valueOf(request.pathVariable("phn"))
        def interest = String.valueOf(request.pathVariable("interest"))

        def event = new PersonInterestAddedEvent(phn, interest)

        iaer.save(event).block()

        kafkaTemplate.send 'mytesttopic', event

        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).build()
    }

    Mono<ServerResponse> showAll(ServerRequest request) {
        ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(prer.findAll(), PersonRegisteredEvent.class)
    }
}
