package wonderland.game.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import wonderland.game.engine.domain.Level;
import wonderland.game.engine.dto.JoystickInputEvent;
import wonderland.game.engine.dto.Movable;

import java.time.Duration;
import java.util.UUID;


@Slf4j
@RestController
@SpringBootApplication
public class GameEngine {

    public static void main(String[] args) {
        SpringApplication.run(GameEngine.class, args);
    }

    private static final String APP_ID = "wonderland.message-publisher";
    private static final String RABBIT_GAME_MESSAGES_EXCHANGE = "messages/game";

    private final AmqpTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public GameEngine(AmqpTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/v1/game/report/input/joystick")
    public void reportGameInput(@RequestBody JoystickInputEvent joystickInputEvent) {
        Level.JOYSTICK_EVENTS.add(joystickInputEvent);
    }

    @PostMapping("/v1/game/state/echo")
    public void reportGameInput(@RequestBody Movable movable) {
        publishGameState("mm7amini@gmail.com", movable);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Flux.interval(Duration.ofMillis(100))
                .flatMap(tick -> Flux.range(0, 5)
                        .scan(Level.level1(), Level::update)
                        .flatMapSequential(level -> Flux.fromStream(level.getMovables()))
                        .doOnNext(movable -> publishGameState("mm7amini@gmail.com", movable)))
                .subscribe();
    }

    public void publishGameState(String receiver, Movable movable) {
        var messageProperties = new MessageProperties();
        messageProperties.setHeader("type", "game_state");
        messageProperties.setHeader("version", "1");
        messageProperties.setMessageId(UUID.randomUUID().toString());
        messageProperties.setAppId(APP_ID);
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setCorrelationId(UUID.randomUUID().toString());
        messageProperties.setReceivedRoutingKey(receiver);
        messageProperties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
        messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
        messageProperties.setReceivedExchange(RABBIT_GAME_MESSAGES_EXCHANGE);
        if (movable.linearVelocityX() == 0 && movable.linearVelocityY() == 0) {
//            return;
        }
        try {
            var body = objectMapper.writeValueAsBytes(movable);
            var message = new Message(body, messageProperties);
            rabbitTemplate.send(RABBIT_GAME_MESSAGES_EXCHANGE, receiver, message);
        } catch (JsonProcessingException e) {
            log.error("problem writeValueAsBytes state", e);
        }
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {

    }
}
