package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class Role implements Serializable {


    long roleId


    String roleName

    String description

    @JsonIgnore
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
        this.description = description
    }

    long getRoleId() {
        return roleId
    }

    void setRoleId(long roleId) {
        this.roleId = roleId
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
