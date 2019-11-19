package wonderland.security.authentication.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonderland.security.authentication.domain.Role;
import wonderland.security.authentication.domain.State;
import wonderland.security.authentication.domain.UserAccount;
import wonderland.security.authentication.dto.UserAccountDto;
import wonderland.security.authentication.exception.RoleNotFoundException;
import wonderland.security.authentication.exception.UserAlreadyExistsException;
import wonderland.security.authentication.exception.UserNotFoundException;
import wonderland.security.authentication.mapper.UserMapper;
import wonderland.security.authentication.repository.RoleRepository;
import wonderland.security.authentication.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("userService")
@Transactional
public class UserServices {

    public static final String USER_ROLE_NAME = "USER";
    public static final Role USER_ROLE = Role.newBuilder().withRoleName(USER_ROLE_NAME).build();
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserServices(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccountDto createNewUserAccount(String email, String phoneNumber, String pass, String name) {
        if (userRepository.findUserByPhoneNumberOrEmail(phoneNumber, email).isPresent()) {
            throw new UserAlreadyExistsException(phoneNumber, email);
        }
        var userRole = roleRepository.findById(USER_ROLE_NAME).orElse(roleRepository.save(USER_ROLE));
        var salt = UUID.randomUUID().toString();
        var mixedPasswordAndSalt = pass + "_" + salt;
        var encodedPassword = passwordEncoder.encode(mixedPasswordAndSalt);
        var userAccount = UserAccount.newBuilder()
                            .withState(State.Active)
                            .withRoles(Set.of(userRole))
                            .withSalt(salt)
                            .withPassword(encodedPassword)
                            .withEmail(email)
                            .withPhoneNumber(phoneNumber)
                            .withName(name).build();
        return UserMapper.map(userRepository.save(userAccount));
    }

    public UserAccountDto getByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(UserMapper::map)
                .orElseThrow(() -> new UserNotFoundException("", email));
    }

    public UserAccountDto getByPhoneNumber(String phoneNumber) {
        return userRepository.findUserByPhoneNumber(phoneNumber)
                .map(UserMapper::map)
                .orElseThrow(() -> new UserNotFoundException(phoneNumber, ""));
    }

    public List<UserAccountDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::map)
                .collect(Collectors.toList());
    }

    public void deleteUser(String phn) {
        userRepository.delete(userRepository.findUserByPhoneNumber(phn)
        .orElseThrow(() ->  new UserNotFoundException(phn, "")));
    }

    public UserAccountDto addRoleToUser(String phoneNumber, String roleName) {
        var role = roleRepository.findById(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        var userAccount = userRepository.findUserByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException(phoneNumber, ""));

        var currentRoles = userAccount.getRoles();
        var newRoles = new HashSet<Role>();
        newRoles.addAll(currentRoles);
        newRoles.add(role);
        var userAccountWithNewRole = userAccount.cloneBuilder().withRoles(newRoles).build();
        return UserMapper.map(userRepository.save(userAccountWithNewRole));
    }
}
