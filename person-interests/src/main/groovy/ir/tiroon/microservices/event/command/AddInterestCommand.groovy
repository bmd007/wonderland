package ir.tiroon.microservices.event.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import ir.tiroon.microservices.event.PersonEvent

import java.time.LocalDateTime

class AddInterestCommand extends PersonEvent{
    String interest

    @JsonCreator
    AddInterestCommand(@JsonProperty("id") UUID id, @JsonProperty("time") LocalDateTime time,
                       @JsonProperty("email") String email,
                       @JsonProperty("interest") String interest) {
        super(id, time, email)
        this.interest = interest
    }

    AddInterestCommand(String email, String interest) {
        super(email)
        this.interest = interest
    }

    String getInterest() {
        return interest
    }

    void setInterest(String interest) {
        this.interest = interest
    }
}
