package wonderland.game.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.j2d.DebugDrawJ2D;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.framework.j2d.TestbedSidePanel;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wonderland.game.engine.domain.Level;
import wonderland.game.engine.domain.Ninja;
import wonderland.game.engine.domain.PhysicalComponent;
import wonderland.game.engine.dto.JoystickInputEvent;
import wonderland.game.engine.dto.Movable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.jbox2d.callbacks.DebugDraw.e_aabbBit;
import static org.jbox2d.callbacks.DebugDraw.e_jointBit;
import static org.jbox2d.callbacks.DebugDraw.e_shapeBit;
import static wonderland.game.engine.domain.PhysicalComponent.WORLD;


@SuppressWarnings("InfiniteLoopStatement")
@Slf4j
@RestController
@SpringBootApplication
public class GameEngine {

    private static final int FPS = 60;

    public static void main(String[] args) {
        new SpringApplicationBuilder(GameEngine.class)
                .web(WebApplicationType.REACTIVE)
                .headless(false)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

    private static final String APP_ID = "wonderland.game-engine";
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
    public void start() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        amqpAdmin.purgeQueue("mm7amini@gmail.com_game");

        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        World world = new World(new Vec2(0, -10));
        TestbedModel model = new TestbedModel();
        MyTestPanelJ2D panel = new MyTestPanelJ2D(model);
        model.setDebugDraw(panel.getDebugDraw());
        world.setDebugDraw(model.getDebugDraw());

        JFrame frame = new JFrame();
        frame.setSize(new Dimension(Float.valueOf(Level.SIZE.x).intValue(), Float.valueOf(Level.SIZE.y).intValue()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("GAME ENGINE");
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        var level1 = Level.level1();
        for(PhysicalComponent physicalComponent : level1.getPhysicalComponents()) {
            var body = world.createBody(physicalComponent.bodyDefinition);
            body.createFixture(physicalComponent.fixtureDefinition);
        }
        var ninja = new Ninja(new Vec2(Level.SIZE.x / 2, Level.SIZE.y / 2), 0);
        var body = world.createBody(ninja.bodyDefinition);
        body.createFixture(ninja.fixtureDefinition);

//        Flux.interval(Duration.ofMillis(50))
//                .subscribeOn(Schedulers.boundedElastic())
//                .doOnNext(ignore -> publishGameState("mm7amini@gmail.com", level1.getNinja().toMovable()))
//                .subscribe();

        while (true) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;
            if (delta >= 1) {
                world.step(Double.valueOf(delta).floatValue(), 8, 3);
                if(panel.render()) {
                    world.drawDebugData();
                    panel.paintScreen();
                }
                delta--;
                drawCount++;
            }

            if (timer > 1_000_000_000) {
//                log.info("FPS: " + drawCount);
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
