package wonderland.api.gateway.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HiRestResource {

    @GetMapping("/rest/hi")
    public Mono<String> hi() {
        return Mono.just("hi rest");
    }
}
