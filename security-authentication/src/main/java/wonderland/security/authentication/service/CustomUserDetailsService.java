package wonderland.security.authentication.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import wonderland.security.authentication.domain.State;
import wonderland.security.authentication.domain.UserAccount;
import wonderland.security.authentication.repository.RoleRepository;
import wonderland.security.authentication.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.*;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    UserRepository userRepository;
    RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Mono<UserDetails> findByUsername(String phoneNumber) throws UsernameNotFoundException {
        return Mono.just(userRepository.findUserByPhoneNumber(phoneNumber))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Username not found")))
                .map(userAccount -> User.builder()
                        .authorities(getGrantedAuthorities(userAccount))
                        .password(userAccount.getPassword())
                        .username(phoneNumber)
                        .accountExpired(false)
                        .accountLocked(userAccount.getState().equals(State.Locked))
                        .disabled(userAccount.getState().equals(State.Disabled))
                        .credentialsExpired(false)
                        .build());
    }

    //TODO here we didn't add the permissions, but they are needed for jwt. Maybe its fine for here?
    private Set<SimpleGrantedAuthority> getGrantedAuthorities(UserAccount userAccount) {
         return userAccount.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(toSet());
    }
}
