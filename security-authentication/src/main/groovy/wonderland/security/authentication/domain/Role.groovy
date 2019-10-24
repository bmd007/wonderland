package wonderland.security.authentication.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.common.base.MoreObjects
import org.hibernate.annotations.Proxy

import javax.persistence.*

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Proxy(lazy = false)
@Table(name = "Role")
@JsonDeserialize(builder = Role.Builder.class)
class Role implements Serializable {

    @Id
    @Column(unique = true, nullable = false)
    String name

    @Column
    String description

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "roles")
    Set<User> users = new HashSet<>()

    Role() {
    }

    String getName() {
        return name
    }

    String getDescription() {
        return description
    }

    Set<User> getUsers() {
        return users
    }

}
