package ir.tiroon.microservices.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime;

class PersonEvent extends Event {

    String email

    @JsonCreator
    PersonEvent(@JsonProperty("id") UUID id, @JsonProperty("time") LocalDateTime time,
                @JsonProperty("email") String email) {
        super(id, time)
        this.email = email
    }

    String getEmail() {
        return email
    }

    void setEmail(String email) {
        this.email = email
    }
}
