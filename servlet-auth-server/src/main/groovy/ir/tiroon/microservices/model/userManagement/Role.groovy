package ir.tiroon.microservices.model.userManagement

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Proxy

import javax.persistence.*

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Proxy(lazy = false)
@Table(name = "Role")
class Role implements Serializable {


    @Id
    @Column(unique = true, nullable = false)
    String roleName

    @Column
    String description

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "roles")
    Set<User> users = new HashSet<>()

    Role() {
    }


    Role(String roleName, String description, Set<User> users) {
        this.roleName = roleName
        this.description = description
        this.users = users
    }

    @JsonCreator
    Role(@JsonProperty("roleName") String roleName,
         @JsonProperty("description") String description) {
        this.roleName = roleName
        this.users = new HashSet<>()
    }

    String getRoleName() {
        return roleName
    }

    void setRoleName(String roleName) {
        this.roleName = roleName
    }

    String getDescription() {
        return description
    }

    void setDescription(String description) {
        this.description = description
    }

    Set<User> getUsers() {
        return users
    }

    void setUsers(Set<User> users) {
        this.users = users
    }
}
