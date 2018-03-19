package ir.tiroon.microservices.ms11.model

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.Table

@Table
class PersonRegisteredEvent extends Event implements Serializable {
//    @PrimaryKeyColumn(name = "eId", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
//    final UUID eventID
//
//    @PrimaryKeyColumn(name = "dateTime", ordinal = 2,
//            type = PrimaryKeyType.PARTITIONED, ordering = Ordering.DESCENDING)
//    final LocalDateTime localDateTime
//
//    @PrimaryKeyColumn(name = "type", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
//    final EventType eventType = EventType.Registered;
//
//    @Column
//    final String relatedPersonPhoneNumber;

    @Column
    final String relatedPersonName;


    PersonRegisteredEvent(String pn, String name) {
        super(pn)
        this.relatedPersonName = name
    }

}