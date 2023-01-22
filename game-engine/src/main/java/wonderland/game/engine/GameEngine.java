package wonderland.game.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import wonderland.game.engine.domain.Level;
import wonderland.game.engine.dto.JoystickInputEvent;
import wonderland.game.engine.dto.Movable;

import java.time.Duration;
import java.util.UUID;


@SuppressWarnings("InfiniteLoopStatement")
@Slf4j
@RestController
@SpringBootApplication
public class GameEngine {

    private static final int FPS = 60;

    public static void main(String[] args) {
        SpringApplication.run(GameEngine.class, args);
    }

    private static final String APP_ID = "wonderland.message-publisher";
    private static final String RABBIT_GAME_MESSAGES_EXCHANGE = "messages/game";

    private final AmqpTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final ObjectMapper objectMapper;

    public GameEngine(AmqpTemplate rabbitTemplate, AmqpAdmin amqpAdmin, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
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
        amqpAdmin.purgeQueue("mm7amini@gmail.com");

        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        Level level1 = Level.level1();

        Flux.interval(Duration.ofMillis(50))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(ignore -> publishGameState("mm7amini@gmail.com", level1.getNinja().toMovable()))
                .subscribe();

        while (true) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;
            if (delta >= 1) {
                level1.update(delta);
                delta--;
                drawCount++;
            }

            if (timer > 1_000_000_000) {
                log.info("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
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
