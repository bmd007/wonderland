package wonderland.security.authentication.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

import java.util.*;

@Service("userService")
@Transactional
public class UserServices {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserServices(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<UserAccountDto> createNewUserAccount(String email, String phoneNumber, String pass, String name) {
        return Mono.just(userRepository.findUserByPhoneNumberOrEmail(phoneNumber, email))
                .filter(Optional::isPresent).map(Optional::get)
                .switchIfEmpty(Mono.error(new UserAlreadyExistsException(phoneNumber, email)))
                .map(userAccount -> {
                    var userRole = roleRepository.findRoleByRoleName("USER").get();
                    var salt = UUID.randomUUID().toString();
                    var mixedPasswordAndSalt = pass + "_" + salt;
                    var encodedPassword = passwordEncoder.encode(mixedPasswordAndSalt);
                    return UserAccount.newBuilder()
                            .withState(State.Active)
                            .withRoles(Set.of(userRole))
                            .withSalt(salt)
                            .withPassword(encodedPassword)
                            .withEmail(email)
                            .withPhoneNumber(phoneNumber)
                            .withName(name).build();
                })
                .map(userRepository::save)
                .map(UserMapper::map);
    }

    public Mono<UserAccountDto> getByEmail(String email) {
        return Mono.just(userRepository.findUserByEmail(email))
                .filter(Optional::isPresent).map(Optional::get)
                .switchIfEmpty(Mono.error(new UserNotFoundException("", email)))
                .map(UserMapper::map);
    }

    public Mono<UserAccountDto> getByPhoneNumber(String phoneNumber) {
        return Mono.just(userRepository.findUserByPhoneNumber(phoneNumber))
                .filter(Optional::isPresent).map(Optional::get)
                .switchIfEmpty(Mono.error(new UserNotFoundException(phoneNumber, "")))
                .map(UserMapper::map);
    }

    public Flux<UserAccountDto> getAllUsers() {
        return Flux.fromIterable(userRepository.findAll())
                .map(UserMapper::map);
    }

    public void deleteUser(String phn) {
        userRepository.delete(userRepository.findUserByPhoneNumber(phn)
        .orElseThrow(() ->  new UserNotFoundException(phn, "")));
    }

    public Mono<UserAccountDto> addRoleToUser(String phoneNumber, String roleName){
        var roleMono = Mono.just(roleRepository.findRoleByRoleName(roleName))
                .filter(Optional::isPresent).map(Optional::get)
                .switchIfEmpty(Mono.error(new RoleNotFoundException(roleName)));

        return Mono.just(userRepository.findUserByPhoneNumber(phoneNumber))
                .filter(Optional::isPresent).map(Optional::get)
                .switchIfEmpty(Mono.error(new UserNotFoundException(phoneNumber, "")))
                .zipWith(roleMono, (userAccount, role) -> {
                    var currentRoles = userAccount.getRoles();
                    var newRoles = new HashSet<Role>();
                    newRoles.addAll(currentRoles);
                    newRoles.add(role);
                    return userAccount.cloneBuilder().withRoles(newRoles).build();
                })
                .map(userAccount -> userRepository.save(userAccount))
                .map(UserMapper::map);
    }
}
