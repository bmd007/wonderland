package wonderland.message.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import wonderland.message.search.repository.MessageRepository;
import wonderland.message.search.domain.Message;
import wonderland.message.search.event.MessageSentEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@SpringBootApplication
public class MessageSearchApplication {

    public static final String MESSAGE_EVENT_TOPIC = "message_events";

    public static void main(String[] args) {
        SpringApplication.run(MessageSearchApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSearchApplication.class);

    @Autowired
    private MessageRepository messageRepository;

    @KafkaListener(topicPattern = MESSAGE_EVENT_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key){
        var sentTime = LocalDateTime.ofInstant(messageSentEvent.getTime(), ZoneId.systemDefault());
        var msg = new Message(messageSentEvent.getFrom(), messageSentEvent.getTo(), messageSentEvent.getBody(), sentTime);
        messageRepository.save(msg).subscribe(message -> LOGGER.info("Message {} saved", message));
    }

    @GetMapping("/message/containing/{text}")
    public Flux<Message> searchAmongMessagesByBody(@PathVariable String text){
        return messageRepository.findByTextContaining(text);
    }

    //todo add a profile calle re indexing that reads the messages from topic from beginning and re fill the elastic index
}


//    private ElasticsearchTemplate elasticsearchTemplate;
//
//    SearchQuery searchQuery = new NativeSearchQueryBuilder()
//            .withQuery(matchAllQuery())
//            .withFilter(boolFilter().must(termFilter("id", documentId)))
//            .build();
//
//    Page<SampleEntity> sampleEntities =
//            elasticsearchTemplate.queryForPage(searchQuery,SampleEntity.class);