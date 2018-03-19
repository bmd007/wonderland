package ir.tiroon.microservices.ms11

import ir.tiroon.microservices.ms11.model.Event
import ir.tiroon.microservices.ms11.model.PersonInterestAddedEvent
import ir.tiroon.microservices.ms11.model.PersonRegisteredEvent
import ir.tiroon.microservices.ms11.repository.PersonRegisteredEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@EnableAsync
class PersonController {

//    @Autowired
//    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    KafkaTemplate<String, Event> kafkaTemplate;

    @Autowired
    PersonRegisteredEventRepository seRepo;


    @GetMapping(path = '/new/person/')
    def register() {

        def se = new PersonRegisteredEvent("09398240640", "Mohammad")

        kafkaTemplate.send('mytesttopic', se)

        seRepo.save(se)

//        applicationEventPublisher.publishEvent(pe);
    }

    @GetMapping(path = '/new/person/{interest}')
    def addInterest(@PathVariable String interest) {

        def inae = new PersonInterestAddedEvent("09398240640", interest)

        kafkaTemplate.send('mytesttopic', inae)

        seRepo.save(inae)
    }

}
