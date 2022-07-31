package wonderland.api.gateway.resource;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class HiResource {

    @MessageMapping("/hi")
    public Mono<String> disLikeADancer() {
        return Mono.just("hi");
    }
}
