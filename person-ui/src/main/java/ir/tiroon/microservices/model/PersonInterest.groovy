package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonProperty

class PersonInterest implements Serializable{

    String phoneNumber

    Set<String> interests = new HashSet<>()

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
