package ir.tiroon.microservices.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@Configuration
class WebSecurityConfig {

    @Autowired
    @Qualifier("customUserDetailsService")
    ReactiveUserDetailsService userDetailsService;

    @Bean
    PasswordEncoder passwordEncoder(){
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/login").permitAll()
                .pathMatchers("/show/users").hasRole("ADMIN")
                .pathMatchers("/hello").hasRole("USER")
                .anyExchange().authenticated().and()
                .formLogin().and()
                .httpBasic().and()
                .authenticationManager(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService))
                .build()
//                .csrf().requireCsrfProtectionMatcher(new PathPatternParserServerWebExchangeMatcher("/login")).disable()
        //loginPage("/login")..failureUrl("/login?error=1").permitAll()
        //.logout().clearAuthentication(true).invalidateHttpSession(true).and()
    }
//    @PreAuthorize("hasRole('ADMIN')")

}