package wonderland.webauthn.webauthnserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class WebauthnServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebauthnServerApplication.class, args);
    }
}
