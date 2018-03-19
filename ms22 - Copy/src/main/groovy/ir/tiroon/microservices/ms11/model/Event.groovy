package ir.tiroon.microservices.ms11.model

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

import java.time.LocalDateTime
import java.time.ZoneId;

@Table
class Event implements Serializable {
    //extends ApplicationEvent

    @PrimaryKeyColumn(name = "eId", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    final UUID eventID

    @PrimaryKeyColumn(name = "dateTime", ordinal = 2,
            type = PrimaryKeyType.PARTITIONED, ordering = Ordering.DESCENDING)
    final LocalDateTime localDateTime

    @PrimaryKeyColumn(name = "type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    final EventType eventType = EventType.InterestAdded;

    @Column
    final String relatedPersonPhoneNumber;


    Event(String pn) {
        this.eventID = UUID.randomUUID()
        this.localDateTime = LocalDateTime.now(ZoneId.systemDefault())
        this.relatedPersonPhoneNumber = pn
    }

}