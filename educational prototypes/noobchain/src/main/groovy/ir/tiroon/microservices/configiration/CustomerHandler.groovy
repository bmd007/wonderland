package ir.tiroon.microservices.configiration



import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CustomerHandler {


//    Mono<ServerResponse> registerPerson(ServerRequest request) {
//        def phn = String.valueOf(request.pathVariable("phn"))
//        def name = String.valueOf(request.pathVariable("name"))
//
//        def event = new PersonRegisteredEvent(phn, name)
//
//        //this is fully reactive
//        //prer.save(event).subscribe()
//
//        //but beacuse we need to send event only after save completion,
//        //we should use block()
//        def savedEvent = prer.save(event).block()
//
//        Message<PersonRegisteredEvent> message = MessageBuilder
//                .withPayload(event)
//                .setHeader(KafkaHeaders.TOPIC, 'mytesttopic6')
//                .build();
//
//        kafkaTemplate.send(message)
//
//        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).
//                body(Mono.just(savedEvent), PersonRegisteredEvent.class)
//    }
//
//
//    Mono<ServerResponse> addInterest(ServerRequest request) {
//        def phn = String.valueOf(request.pathVariable("phn"))
//        def interest = String.valueOf(request.pathVariable("interest"))
//
//        def event = new PersonInterestAddedEvent(phn, interest)
//
//        def savedEvent = iaer.save(event).block()
//
//        Message<PersonInterestAddedEvent> message = MessageBuilder
//                .withPayload(event)
//                .setHeader(KafkaHeaders.TOPIC, 'mytesttopic7')
//                .build();
//
//        kafkaTemplate.send(message)
//
//        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).
//                body(Mono.just(savedEvent), PersonInterestAddedEvent.class)
//    }


}
