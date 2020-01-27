package wonderland.communication.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import wonderland.communication.graph.domain.Communication;
import wonderland.communication.graph.domain.Person;
import wonderland.communication.graph.event.MessageSentEvent;
import wonderland.communication.graph.repository.CommunicationRepository;
import wonderland.communication.graph.repository.PersonRepository;

@RestController
@SpringBootApplication
public class CommunicationGraohApplication {

    public static final String MESSAGE_EVENT_TOPIC = "message_events";

    public static void main(String[] args) {
        SpringApplication.run(CommunicationGraohApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationGraohApplication.class);

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CommunicationRepository communicationRepository;

    @KafkaListener(topicPattern = MESSAGE_EVENT_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key){
        Person sender = personRepository.findById(messageSentEvent.getFrom())
                .orElse(personRepository.save(new Person(messageSentEvent.getFrom())));
        Person receiver = personRepository.findById(messageSentEvent.getTo())
                .orElse(personRepository.save(new Person(messageSentEvent.getTo())));

        var communication = new Communication(sender, receiver, messageSentEvent.getTime());
        var savedCommunication = communicationRepository.save(communication);
        LOGGER.info("communication {} saved", savedCommunication);
    }

    //todo recommendation: if A talked to B and C talked to B and D talked to B, when E talks to A and C and D, system recommends to E to talk to B
    //todo find a ring of people who talked to each other one by one and suggest them to create a group
    //todo calculate the most and least popular person
    //todo calculate leaders (nodes with the most shortest paths to others) =? Most influential person
    //todo calculate group connectors
    //https://neo4j.com/blog/graph-algorithms-neo4j-15-different-graph-algorithms-and-what-they-do/

}
