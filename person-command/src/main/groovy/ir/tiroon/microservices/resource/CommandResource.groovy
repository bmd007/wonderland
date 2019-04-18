package ir.tiroon.microservices.resource

import ir.tiroon.microservices.event.command.AddInterestCommand
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/person/command")
class CommandResource {

    KafkaTemplate kafkaTemplate

    CommandResource(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate
    }

    @PostMapping("/interest/{interest}/{email}")
    Void addInterest(@PathVariable String interest, @PathVariable String email){
            kafkaTemplate.send("add-interest-command", new AddInterestCommand(email, interest))
    }
}
