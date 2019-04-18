package ir.tiroon.microservices.service


import ir.tiroon.microservices.model.userManagement.Role
import ir.tiroon.microservices.model.userManagement.State
import ir.tiroon.microservices.model.userManagement.User
import ir.tiroon.microservices.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono


@Service("customUserDetailsService")
class CustomUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    UserRepository userRepository

    @Transactional(readOnly = true)
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        def userOptional = userRepository.findById(email)
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Username not found")
        }

        User user = userOptional.get()

        new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPassword(),
                user.getState() == State.Active,
                true,
                true,
                true,
                getGrantedAuthorities(user)
            )
    }

    private List<GrantedAuthority> getGrantedAuthorities(User user) {
        def authorities = new ArrayList<GrantedAuthority>()

        for (Role r : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getRoleName()))
        }
        authorities
    }

    @Override
    Mono<UserDetails> findByUsername(String username) {
        Mono.just(loadUserByUsername(username))
    }
}
