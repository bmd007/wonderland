package wonderland.security.authentication.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Proxy(lazy = false)
@Table(name = "Role")
@JsonDeserialize(builder = Role.Builder.class)
public class Role {

    @Id
    @Column(unique = true, nullable = false)
    private String roleName;

    @Column
    private String description;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "roles", cascade = CascadeType.DETACH)
    private Set<UserAccount> userAccounts;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private Set<Permission> permissions;

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public Set<UserAccount> getUserAccounts() {
        return userAccounts;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roleName", roleName)
                .add("description", description)
                .add("users", userAccounts)
                .add("permissions", permissions)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equal(getRoleName(), role.getRoleName()) &&
                Objects.equal(getDescription(), role.getDescription()) &&
                Objects.equal(getUserAccounts(), role.getUserAccounts()) &&
                Objects.equal(getPermissions(), role.getPermissions());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getRoleName(), getDescription(), getUserAccounts(), getPermissions());
    }

    public Builder cloneBuilder(){
        return new Builder()
                .withDescription(description)
                .withRoleName(roleName)
                .withPermissions(permissions)
                .withUserAccounts(userAccounts);
    }

    public static class Builder {
        private String roleName;
        private String description;
        private Set<UserAccount> userAccounts;
        private Set<Permission> permissions;

        private Builder() {
        }

        public static Builder role() {
            return new Builder();
        }

        public Builder withRoleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withUserAccounts(Set<UserAccount> userAccounts) {
            this.userAccounts = userAccounts;
            return this;
        }

        public Builder withPermissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Role build() {
            return new Role();
        }
    }
}
