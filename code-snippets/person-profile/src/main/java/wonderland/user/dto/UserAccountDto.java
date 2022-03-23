package wonderland.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Set;

@JsonDeserialize(builder = UserAccountDto.Builder.class)
public class UserAccountDto {

    String name;
    String email;
    String phoneNumber;
    Set<String> roleNames;

    private UserAccountDto(Builder builder) {
        name = builder.name;
        email = builder.email;
        phoneNumber = builder.phoneNumber;
        roleNames = builder.roleNames;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(UserAccountDto copy) {
        Builder builder = new Builder();
        builder.name = copy.getName();
        builder.email = copy.getEmail();
        builder.phoneNumber = copy.getPhoneNumber();
        builder.roleNames = copy.getRoleNames();
        return builder;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("email", email)
                .add("phoneNumber", phoneNumber)
                .add("roleNames", roleNames)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccountDto that = (UserAccountDto) o;
        return Objects.equal(getName(), that.getName()) &&
                Objects.equal(getEmail(), that.getEmail()) &&
                Objects.equal(getPhoneNumber(), that.getPhoneNumber()) &&
                Objects.equal(getRoleNames(), that.getRoleNames());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getEmail(), getPhoneNumber(), getRoleNames());
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public static final class Builder {
        private String name;
        private String email;
        private String phoneNumber;
        private Set<String> roleNames;

        private Builder() {
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public Builder withEmail(String val) {
            email = val;
            return this;
        }

        public Builder withPhoneNumber(String val) {
            phoneNumber = val;
            return this;
        }

        public Builder withRoleNames(Set<String> val) {
            roleNames = val;
            return this;
        }

        public UserAccountDto build() {
            return new UserAccountDto(this);
        }
    }
}
