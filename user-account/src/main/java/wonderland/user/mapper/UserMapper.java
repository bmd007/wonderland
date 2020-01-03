package wonderland.user.mapper;

import wonderland.user.domain.Role;
import wonderland.user.domain.UserAccount;
import wonderland.user.dto.UserAccountDto;

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
