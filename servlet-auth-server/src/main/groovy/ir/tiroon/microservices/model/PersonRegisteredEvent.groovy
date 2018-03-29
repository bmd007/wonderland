package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class PersonRegisteredEvent {

    EventKey key;

    String relatedPersonName;

    PersonRegisteredEvent() {
    }

    @JsonCreator
    PersonRegisteredEvent(@JsonProperty("key") final EventKey key,
                          @JsonProperty("relatedPersonName") final String relatedPersonName) {
        this.key = key
        this.relatedPersonName = relatedPersonName
    }


    PersonRegisteredEvent(final String phoneNumber, final String relatedPersonName) {
        def key = new EventKey(EventType.Registered.toString(), phoneNumber)
        this.key = key
        this.relatedPersonName = relatedPersonName
    }

    EventKey getKey() {
        return key
    }

    void setKey(EventKey key) {
        this.key = key
    }

    String getRelatedPersonName() {
        return relatedPersonName
    }

    void setRelatedPersonName(String relatedPersonName) {
        this.relatedPersonName = relatedPersonName
    }
}