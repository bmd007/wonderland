package ir.tiroon.microservices.ms11.repository

import ir.tiroon.microservices.ms11.model.PersonRegisteredEvent
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PersonRegisteredEventRepository extends ReactiveCassandraRepository<PersonRegisteredEvent, UUID> {
    Flux<PersonRegisteredEvent> findAll()
}