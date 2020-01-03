package wonderland.hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloResource {

    @Value("${wonderland.hello.prefix}")
    String helloPrefix;

    @GetMapping
    public Mono<String> hello() {
        return Mono.just(helloPrefix + " Hello");
    }
}
