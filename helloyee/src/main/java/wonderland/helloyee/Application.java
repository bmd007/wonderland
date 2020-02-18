package wonderland.helloyee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetAddress;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Value("${hello.world.ip}") String halloWorldIp;

    @Autowired
    @Qualifier("loadBalancedClient")
    WebClient.Builder loadBalancedWebClientBuilder;

    @Autowired
    @Qualifier("notLoadBalancedClient")
    WebClient.Builder notLoadBalancedWebClientBuilder;

    @Override
    public void run(String... args) throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println("IP Address:- " + inetAddress.getHostAddress());
        System.out.println("Host Name:- " + inetAddress.getHostName());

        notLoadBalancedWebClientBuilder.build()
                .get()
                .uri(halloWorldIp)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> LOGGER.error("not load balanced error:{}", throwable.getMessage()))
                .subscribe(s -> System.out.println("Answer got by NOT loadBalancedClient from hello_world : "+ s));

        loadBalancedWebClientBuilder.build()
                .get()
                .uri(halloWorldIp)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> LOGGER.error("load balanced error:{}",throwable.getMessage()))
                .subscribe(s -> System.out.println("Answer got by loadBalancedClient from hello_world : "+ s));
    }
}
