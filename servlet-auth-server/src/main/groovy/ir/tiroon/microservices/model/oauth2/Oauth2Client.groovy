package ir.tiroon.microservices.model.oauth2

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Proxy

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "Oauth2Client")
@Proxy(lazy = false)
class Oauth2Client implements Serializable{

    @Id
    @Column(nullable = false, unique = true)
    String id

    @Column(nullable = false)
    String secret

    @JsonCreator
    Oauth2Client(@JsonProperty("id")String id,@JsonProperty("secret") String secret) {
        this.id = id
        this.secret = secret
    }

    Oauth2Client() {
    }

    String getId() {
        return id
    }

    void setId(String id) {
        this.id = id
    }

    String getSecret() {
        return secret
    }

    void setSecret(String secret) {
        this.secret = secret
    }
}
