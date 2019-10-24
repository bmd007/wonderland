package wonderland.security.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableWebFluxSecurity
@SpringBootApplication
public class SecurityAuthenticationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityAuthenticationApplication.class, args);
    }
}
