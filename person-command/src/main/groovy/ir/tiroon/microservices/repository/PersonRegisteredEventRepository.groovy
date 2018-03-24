package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.EventKey
import ir.tiroon.microservices.model.PersonRegisteredEvent
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRegisteredEventRepository extends
        ReactiveCassandraRepository<PersonRegisteredEvent, EventKey> {}
