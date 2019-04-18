package ir.tiroon.microservices.resource

import ir.tiroon.microservices.repository.PersonInterestRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/interest")
class InterestResource {

    PersonInterestRepository interestRepository

    InterestResource(PersonInterestRepository interestRepository) {
        this.interestRepository = interestRepository
    }

    @GetMapping("/{email}")
    Flux<String> getInterestsByEmail(@PathVariable String email){
        interestRepository.findById(email).flatMapIterable{
            interest -> interest.interests
        }
    }
}
