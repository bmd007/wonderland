package ir.tiroon.microservices.personcommand.model

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn

import java.time.LocalDateTime
import java.time.ZoneId;

@PrimaryKeyClass
class EventKey implements Serializable{

    @PrimaryKeyColumn(name = "eventId",type = PrimaryKeyType.CLUSTERED, ordinal = 1, ordering = Ordering.DESCENDING)
    UUID eventID

    @PrimaryKeyColumn(name = "dateTime", ordinal = 0, ordering = Ordering.DESCENDING)
    LocalDateTime localDateTime

    @PrimaryKeyColumn(name = "eventType", ordinal = 0, ordering = Ordering.DESCENDING)
    String eventType

    @PrimaryKeyColumn(name = "phoneNumber", type = PrimaryKeyType.PARTITIONED)
    String phoneNumber;

    EventKey(final String eventType,final String phoneNumber) {
        this.eventID = UUID.randomUUID()
        this.localDateTime = LocalDateTime.now(ZoneId.systemDefault())
        this.eventType = eventType
        this.phoneNumber = phoneNumber
    }


    void setEventID(UUID eventID) {
        this.eventID = eventID
    }

    void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime
    }

    void setEventType(String eventType) {
        this.eventType = eventType
    }


    String getPhoneNumber() {
        return phoneNumber
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof EventKey)) return false

        EventKey eventKey = (EventKey) o

        if (eventID != eventKey.eventID) return false
        if (eventType != eventKey.eventType) return false
        if (localDateTime != eventKey.localDateTime) return false
        if (relatedPersonPhoneNumber != eventKey.relatedPersonPhoneNumber) return false

        return true
    }

    int hashCode() {
        int result
        result = (eventID != null ? eventID.hashCode() : 0)
        result = 31 * result + (localDateTime != null ? localDateTime.hashCode() : 0)
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0)
        result = 31 * result + (relatedPersonPhoneNumber != null ? relatedPersonPhoneNumber.hashCode() : 0)
        return result
    }

    UUID getEventID() {
        return eventID
    }

    LocalDateTime getLocalDateTime() {
        return localDateTime
    }

    String getEventType() {
        return eventType
    }

}
