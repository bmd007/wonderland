package wonderland.api.gateway.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class HiRestResource {

    @GetMapping("/rest/hi")
    public Mono<String> disLikeADancer() {
        log.info("say hi resr");
        return Mono.just("hi rest");
    }
}
