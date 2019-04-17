package ir.tiroon.microservices.event

import java.time.LocalDateTime

class Event {
    UUID eventID

    //TODO check what type of time is suitable here
    LocalDateTime localDateTime
}
