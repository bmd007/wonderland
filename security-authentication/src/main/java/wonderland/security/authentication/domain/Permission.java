package wonderland.security.authentication.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Proxy(lazy = false)
@Table(name = "Permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column
    private String application;

    @Column
    private String name;

    @Override
    public String toString() {
        return application+":"+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equal(getId(), that.getId()) &&
                Objects.equal(getApplication(), that.getApplication()) &&
                Objects.equal(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), getApplication(), getName());
    }

    public Long getId() {
        return id;
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
}
