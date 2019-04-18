package ir.tiroon.microservices.repository


import ir.tiroon.microservices.model.PersonInterests
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PersonInterestRepository extends ReactiveMongoRepository<PersonInterests, String> {

}