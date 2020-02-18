package wonderland.helloyee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetAddress;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Value("${wonderland.hello.prefix}") Object configurationValue;

    @Autowired
    WebClient.Builder webClientBuilder;

    @Override
    public void run(String... args) throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println("IP Address:- " + inetAddress.getHostAddress());
        System.out.println("Host Name:- " + inetAddress.getHostName());

        System.out.println("configuration value:- " + configurationValue);

        webClientBuilder.build()
                .get()
                .uri("http://hello_world")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(s -> System.out.println("Answer from hello_world: "+ s));

    }
}
