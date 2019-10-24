package wonderland.security.authentication.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class PermissionDto {
    private String name;
    private String application;

    @JsonCreator
    public PermissionDto(@JsonProperty("name") String name, @JsonProperty("application") String application) {
        this.name = name;
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionDto that = (PermissionDto) o;
        return Objects.equal(getName(), that.getName()) &&
                Objects.equal(getApplication(), that.getApplication());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getApplication());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("application", application)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
