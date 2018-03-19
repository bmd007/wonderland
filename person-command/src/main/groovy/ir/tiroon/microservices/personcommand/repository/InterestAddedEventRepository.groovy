package ir.tiroon.microservices.personcommand.repository

import ir.tiroon.microservices.personcommand.model.EventKey
import ir.tiroon.microservices.personcommand.model.PersonInterestAddedEvent
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface InterestAddedEventRepository extends
        ReactiveCassandraRepository<PersonInterestAddedEvent, EventKey> {
}
