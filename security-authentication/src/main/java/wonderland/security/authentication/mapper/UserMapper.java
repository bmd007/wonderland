package wonderland.security.authentication.mapper;

import wonderland.security.authentication.domain.Role;
import wonderland.security.authentication.domain.UserAccount;
import wonderland.security.authentication.dto.UserAccountDto;

import static java.util.stream.Collectors.toSet;

public class UserMapper {
    public static UserAccountDto map(UserAccount userAccount) {
        return UserAccountDto.newBuilder().withEmail(userAccount.getEmail())
                .withPhoneNumber(userAccount.getPhoneNumber())
                .withRoleNames(userAccount.getRoles().stream().map(Role::getRoleName).collect(toSet()))
                .withName(userAccount.getName())
                .build();
    }
}
