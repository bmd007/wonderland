package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.EventKey
import ir.tiroon.microservices.model.PersonInterestAddedEvent
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface InterestAddedEventRepository extends
        ReactiveCassandraRepository<PersonInterestAddedEvent, EventKey> {
}
