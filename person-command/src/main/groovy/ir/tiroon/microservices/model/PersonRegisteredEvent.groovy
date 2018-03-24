package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table
class PersonRegisteredEvent {

    @PrimaryKey
    EventKey key;

    @Column
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