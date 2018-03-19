package ir.tiroon.microservices.personcommand.model

import java.time.LocalDateTime
import java.time.ZoneId;

class Event {
    final UUID eventID

    final LocalDateTime localDateTime

    final String eventType

    final String relatedPersonPhoneNumber;

    Event(String eventType, String relatedPersonPhoneNumber) {
        this.eventID = UUID.randomUUID()
        this.localDateTime = LocalDateTime.now(ZoneId.systemDefault())
        this.eventType = eventType
        this.relatedPersonPhoneNumber = relatedPersonPhoneNumber
    }
}
