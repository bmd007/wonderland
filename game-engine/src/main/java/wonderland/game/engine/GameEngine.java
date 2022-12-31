package wonderland.game.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class GameEngine {

    public static void main(String[] args) {
        SpringApplication.run(GameEngine.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {

    }
}
