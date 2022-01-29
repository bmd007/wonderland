package wonderland.game.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
public class RSocketRocketProducer {

    @MessageMapping("rocket")
    public Flux<String> sendRockets(String payload){
        System.out.println(payload);
        List<String> values = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .map(String::valueOf)
                .collect(Collectors.toList());

        return Flux.interval(Duration.ofNanos(1200))
                .map(tick -> (new Random().nextDouble() * values.size()))
                .map(Double::intValue)
                .map(values::get)
                .map(random -> random+":"+random)
                .doOnError(error -> log.error("Rocket production error: {}", error.getMessage()));
    }

    record RocketRequest(int x, int y, int length, int howMany){ }

    record Rocket(int x, int y){ }
}
