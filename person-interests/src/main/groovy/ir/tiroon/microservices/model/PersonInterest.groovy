package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "PersonInterest")
class PersonInterest implements Serializable{

    @Id
    String phoneNumber

    Set<String> interests = new HashSet<>()

    @JsonCreator
    PersonInterest(@JsonProperty("phoneNumber") String phoneNumber,
           @JsonProperty("interests") Set<String> interests) {
        this.phoneNumber = phoneNumber
        this.interests = interests
    }

    PersonInterest(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }

    PersonInterest() {
    }

    void addInterest(String interest){
        interests.add(interest)
    }

    String getPhoneNumber() {
        return phoneNumber
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }

    Set<String> getInterests() {
        return interests
    }

    void setInterests(Set<String> interests) {
        this.interests = interests
    }
}
