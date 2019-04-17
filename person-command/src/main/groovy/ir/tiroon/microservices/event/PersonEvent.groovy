package ir.tiroon.microservices.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime;

class PersonEvent extends Event {

    UUID userId

    @JsonCreator
    PersonEvent(@JsonProperty("id") UUID id, @JsonProperty("time") LocalDateTime time, @JsonProperty("userId") UUID userId) {
        super(id, time)
        this.userId = userId
    }
}
