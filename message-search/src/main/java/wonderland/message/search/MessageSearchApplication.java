package wonderland.message.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import wonderland.message.search.domain.Message;
import wonderland.message.search.event.MessageSentEvent;
import wonderland.message.search.repository.MessageRepository;


@RestController
@SpringBootApplication
public class MessageSearchApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSearchApplication.class);

    public static final String MESSAGES_EVENTS_TOPIC = "messageEvents";

    public static void main(String[] args) {
        SpringApplication.run(MessageSearchApplication.class, args);
    }

    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topicPattern = MESSAGES_EVENTS_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        var msg = Message.define(messageSentEvent.from(), messageSentEvent.to(), messageSentEvent.body(), messageSentEvent.time());
        messageRepository.save(msg).subscribe(message -> LOGGER.info("Message {} saved", message));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

    }

    @GetMapping("/message/containing/{text}")
    public Flux<Message> searchAmongMessagesByBody(@PathVariable String text) {
        return messageRepository.findByTextContaining(text);
    }

    //todo add a profile call re indexing that reads the messages from topic from beginning and re fill the elastic index
}
