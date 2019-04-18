package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Person")
class Person implements Serializable{

    @Id
    @Column(unique = true, nullable = false)
    String phoneNumber

    @Column(nullable = false)
    String name

    @JsonCreator
    Person(@JsonProperty("phoneNumber") String phoneNumber,@JsonProperty("name") String name) {
        this.phoneNumber = phoneNumber
        this.name = name
    }

    Person() {
    }

    String getPhoneNumber() {
        return phoneNumber
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }


}
