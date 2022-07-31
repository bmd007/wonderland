package wonderland.api.gateway.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
public class HiResource {

    @MessageMapping("/hi")
    public Mono<String> disLikeADancer() {
        log.info("say hi");
        return Mono.just("hi");
    }
}
