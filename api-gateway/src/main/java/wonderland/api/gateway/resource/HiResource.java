package wonderland.api.gateway.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wonderland.api.gateway.config.Topics;
import wonderland.api.gateway.dto.Location;
import wonderland.api.gateway.dto.WonderSeekerDto;
import wonderland.api.gateway.dto.WonderSeekerLikesDto;
import wonderland.api.gateway.dto.WonderSeekerMatchesDto;
import wonderland.api.gateway.dto.WonderSeekersDto;
import wonderland.api.gateway.event.DancePartnerSeekerHasLikedAnotherDancerEvent;
import wonderland.api.gateway.event.DancePartnerSeekerIsDisLikedEvent;
import wonderland.api.gateway.event.DancerIsLookingForPartnerUpdate;
import wonderland.api.gateway.event.Event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
public class HiResource {

    @MessageMapping("/hi")
    public Mono<String> disLikeADancer() {
        log.info("say hi");
        return Mono.just("hi");
    }
}
