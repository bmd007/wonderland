package wonderland.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class CheckResource {

    @Autowired
    WebClient.Builder webClientBuilder;

    @GetMapping
    public Mono<String> check() {
        return webClientBuilder.build()
                .get()
                .uri("http://hello")
                .retrieve()
                .bodyToMono(String.class);
    }
}
