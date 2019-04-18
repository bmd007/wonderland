package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "PersonInterests")
class PersonInterests implements Serializable{

    @Id
    String email

    Set<String> interests = new HashSet<>()

    @JsonCreator
    PersonInterests(@JsonProperty("email") String email,
                    @JsonProperty("interests") Set<String> interests) {
        this.email = email
    }

    PersonInterests(String email) {
        this.email = email
    }

    PersonInterests() {
    }

    void addInterest(String interest){
        interests.add(interest)
    }

    String getPhoneNumber() {
        return email
    }

    void setPhoneNumber(String phoneNumber) {
        this.email = phoneNumber
    }

    Set<String> getInterests() {
        return interests
    }

    void setInterests(Set<String> interests) {
        this.interests = interests
    }
}
