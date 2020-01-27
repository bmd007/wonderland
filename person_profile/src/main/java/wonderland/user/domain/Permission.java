package wonderland.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Proxy(lazy = false)
@Table(name = "Permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long permissionId;

    @Column
    private String application;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "permissions", cascade = CascadeType.DETACH)
    private Set<Role> roles = new HashSet<>();

    public Permission(String application, String name, Set<Role> roles) {
        this.application = application;
        this.name = name;
        this.roles = roles;
    }

    public Permission() {
    }

    @Override
    public String toString() {
        return application + ":" + name + ":" + roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionId.equals(that.permissionId) &&
                application.equals(that.application) &&
                name.equals(that.name) &&
                Objects.equals(roles, that.roles);
    }

    public String getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public Permission(String application, String name) {
        this.application = application;
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setName(String name) {
        this.name = name;
    }
}
