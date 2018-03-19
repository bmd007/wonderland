package ir.tiroon.microservices.personcommand.repository

import ir.tiroon.microservices.personcommand.model.EventKey
import ir.tiroon.microservices.personcommand.model.PersonRegisteredEvent
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRegisteredEventRepository extends
        ReactiveCassandraRepository<PersonRegisteredEvent, EventKey> {}
