package wonderland.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import org.hibernate.annotations.Proxy;

import jakarta.persistence.*;
import java.util.Objects;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rolePermissions",
            joinColumns = @JoinColumn(name = "roleName"),
            inverseJoinColumns = @JoinColumn(name = "permissionId"))
    private Set<Permission> permissions;

    public Role() {
    }

    public Role(String roleName, String description, Set<UserAccount> userAccounts, Set<Permission> permissions) {
        this.roleName = roleName;
        this.description = description;
        this.userAccounts = userAccounts;
        this.permissions = permissions;
    }

    private Role(Builder builder) {
        roleName = builder.roleName;
        description = builder.description;
        userAccounts = builder.userAccounts;
        permissions = builder.permissions;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Role copy) {
        Builder builder = new Builder();
        builder.roleName = copy.getRoleName();
        builder.description = copy.getDescription();
        builder.userAccounts = copy.getUserAccounts();
        builder.permissions = copy.getPermissions();
        return builder;
    }

    public Builder cloneBuilder() {
        return newBuilder(this);
    }

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
        return roleName.equals(role.roleName) &&
                Objects.equals(description, role.description) &&
                Objects.equals(userAccounts, role.userAccounts) &&
                Objects.equals(permissions, role.permissions);
    }

    public static final class Builder {
        private String roleName;
        private String description;
        private Set<UserAccount> userAccounts;
        private Set<Permission> permissions;

        private Builder() {
        }

        public Builder withRoleName(String val) {
            roleName = val;
            return this;
        }

        public Builder withDescription(String val) {
            description = val;
            return this;
        }

        public Builder withUserAccounts(Set<UserAccount> val) {
            userAccounts = val;
            return this;
        }

        public Builder withPermissions(Set<Permission> val) {
            permissions = val;
            return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}
