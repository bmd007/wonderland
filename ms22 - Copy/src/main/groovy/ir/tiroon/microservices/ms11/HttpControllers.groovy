package ir.tiroon.microservices.personcommand.configiration

import ir.tiroon.microservices.personcommand.model.PersonInterestAddedEvent
import ir.tiroon.microservices.personcommand.model.PersonRegisteredEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
class HttpControllers {

    @Autowired
    KafkaTemplate kafkaTemplate

    @GetMapping(path = '/new/person/{phn}/{name}')
    ResponseEntity register(@PathVariable("phn") String phn, @PathVariable("name") String name) {
        kafkaTemplate.send 'mytesttopic', new PersonRegisteredEvent(phn, name)
        new ResponseEntity(HttpStatus.OK)
    }

    @GetMapping(path = '/new/interest/{phn}/{interest}')
    ResponseEntity addInterest(@PathVariable("phn") String phn, @PathVariable("interest") String interest) {
        kafkaTemplate.send 'mytesttopic', new PersonInterestAddedEvent(phn, interest)
        new ResponseEntity(HttpStatus.OK)
    }

}
