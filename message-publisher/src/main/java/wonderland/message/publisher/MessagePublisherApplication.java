package wonderland.message.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class MessagePublisherApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePublisherApplication.class);

//    @Bean
//    public TimedAspect timedAspect(MeterRegistry registry) {
//        return new TimedAspect(registry);
//    }

    public static void main(String[] args) {
        SpringApplication.run(MessagePublisherApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {

    }
}
