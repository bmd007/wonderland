package wonderland.message.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@SpringBootApplication
public class MessageSearchApplication {

    public static final String MESSAGE_EVENT_TOPIC = "message_events";

    public static void main(String[] args) {
        SpringApplication.run(MessageSearchApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSearchApplication.class);

    @KafkaListener(topicPattern = MESSAGE_EVENT_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent event, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key){
        LOGGER.info("Message {} sent from {} to {} at {}", event.body, event.from, event.to, event.time);
    }

}
