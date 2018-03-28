package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime
import java.time.ZoneId

class EventKey implements Serializable{

    UUID eventID

    LocalDateTime localDateTime

    String eventType

    String phoneNumber;

    EventKey() {
    }

    @JsonCreator
    EventKey(@JsonProperty("eventID") UUID eventID,
             @JsonProperty("localDateTime") LocalDateTime localDateTime,
             @JsonProperty("eventType") String eventType,
             @JsonProperty("phoneNumber") String phoneNumber) {
        this.eventID = eventID
        this.localDateTime = localDateTime
        this.eventType = eventType
        this.phoneNumber = phoneNumber
    }

    EventKey(final String eventType, final String phoneNumber) {
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
