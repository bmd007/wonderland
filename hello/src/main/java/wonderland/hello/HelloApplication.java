package wonderland.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class HelloApplication implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloApplication.class);

    @Value("${spring.cloud.config.uri}")
    String configServerRui;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("config server is at {}", configServerRui);
    }
}
