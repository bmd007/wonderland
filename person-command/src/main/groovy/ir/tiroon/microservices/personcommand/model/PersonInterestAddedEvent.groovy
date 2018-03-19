package ir.tiroon.microservices.personcommand.model

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table
class PersonInterestAddedEvent {

    @PrimaryKey
    EventKey key;

    @Column
    String InterestName;

    PersonInterestAddedEvent() {
    }

    PersonInterestAddedEvent(EventKey key, String interestName) {
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
