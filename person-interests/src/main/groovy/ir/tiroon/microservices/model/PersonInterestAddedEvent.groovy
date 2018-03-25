package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class PersonInterestAddedEvent {

    EventKey key;

    String InterestName;

    PersonInterestAddedEvent() {
    }

    @JsonCreator
    PersonInterestAddedEvent(@JsonProperty("key") final EventKey key,
                             @JsonProperty("interestName") final String interestName) {
        this.key = key
        InterestName = interestName
    }

    PersonInterestAddedEvent(final String relatedPersonPhoneNumber, final String interestName) {
        def key = new EventKey(EventType.InterestAdded.toString(), relatedPersonPhoneNumber)
        this.key = key
        InterestName = interestName
    }

    EventKey getKey() {
        return key
    }

    void setKey(EventKey key) {
        this.key = key
    }

    String getInterestName() {
        return InterestName
    }

    void setInterestName(String interestName) {
        InterestName = interestName
    }
}
