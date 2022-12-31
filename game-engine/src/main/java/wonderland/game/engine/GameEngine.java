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
import wonderland.game.engine.dto.GameState;
import wonderland.game.engine.dto.JoystickInputEvent;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


@Slf4j
@RestController
@SpringBootApplication
public class GameEngine {

    public static void main(String[] args) {
        SpringApplication.run(GameEngine.class, args);
    }

    private static final String APP_ID = "wonderland.message-publisher";
    private static final String RABBIT_GAME_MESSAGES_EXCHANGE = "messages/game";
    private static final ConcurrentLinkedQueue<JoystickInputEvent> JOYSTICK_EVENTS = new ConcurrentLinkedQueue<>();

    private final AmqpTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public GameEngine(AmqpTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/v1/game/report/input/joystick")
    public void reportGameInput(@RequestBody JoystickInputEvent joystickInputEvent) {
        JOYSTICK_EVENTS.add(joystickInputEvent);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Flux.interval(Duration.ofMillis(100))
                .flatMapIterable(tick -> JOYSTICK_EVENTS.stream().limit(10).toList())
                .scan(GameState.createState(0).addNinjaInitially(), (state, joystickInputEvent) -> state)
                .doOnNext(state -> publishGameState("mm7amini@gmail.com", state))
                .subscribe();
    }

    public void publishGameState(String receiver, GameState state) {
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
        Message message = null;
        try {
            message = new Message(objectMapper.writeValueAsBytes(state), messageProperties);
        } catch (JsonProcessingException e) {
            log.error("problem writeValueAsBytes state", e);
        }
        rabbitTemplate.send(RABBIT_GAME_MESSAGES_EXCHANGE, receiver, message);
        log.info("game state {} sent to {}", state, receiver);
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {

    }
}
