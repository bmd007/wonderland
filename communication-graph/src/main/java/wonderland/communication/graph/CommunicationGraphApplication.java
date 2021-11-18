package wonderland.communication.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import wonderland.communication.graph.config.Topics;
import wonderland.communication.graph.domain.Communication;
import wonderland.communication.graph.domain.Person;
import wonderland.communication.graph.event.MessageSentEvent;
import wonderland.communication.graph.repository.CommunicationRepository;
import wonderland.communication.graph.repository.PersonRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EnableNeo4jRepositories("wonderland.*")
@RestController
@SpringBootApplication
public class CommunicationGraphApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationGraphApplication.class);
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private CommunicationRepository communicationRepository;

    public static void main(String[] args) {
        SpringApplication.run(CommunicationGraphApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        personRepository.deleteAll();
        communicationRepository.deleteAll();
        var p1 = new Person(null, "p1", List.of());
        var p2 = new Person(null, "p2", List.of());
        var p3 = new Person(null, "p3", List.of());
        p1 = personRepository.save(p1);
        p2 = personRepository.save(p2);
        p3 = personRepository.save(p3);
        System.out.println(personRepository.findAll());
        var communication = new Communication(null, p1, p2, Instant.now());
        var communication2 = new Communication(null, p1, p3, Instant.now());
//        p1.addCommunication(communication);
//        p1.addCommunication(communication2);
        communication = communicationRepository.save(communication);
        communication2 = communicationRepository.save(communication2);
        System.out.println(communicationRepository.findAll());
//        System.out.println(communication2);
//        p1 = p1.addCommunication(communication);
//        p1 = p1.addCommunication(communication2);
//        personRepository.save(p1);
    }

//    @KafkaListener(topicPattern = Topics.MESSAGES_EVENTS_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        try {
            Person sender = personRepository.findByEmail(messageSentEvent.from())
                    .orElseGet(() -> personRepository.save(Person.of(messageSentEvent.from())));
            LOGGER.info("sender {} saved", sender);

            Person receiver = personRepository.findByEmail(messageSentEvent.to())
                    .orElseGet(() -> personRepository.save(Person.of(messageSentEvent.to())));
            LOGGER.info("receiver {} receiver", receiver);

            var communication = Communication.defineNew(sender, receiver, messageSentEvent.time());
            var savedCommunication = communicationRepository.save(communication);

            LOGGER.info("communication {} saved", savedCommunication);
        } catch (Exception e) {
            LOGGER.error("Problem {} while processing {}", e.getMessage(), messageSentEvent);
        }
    }

    //todo recommendation: if A talked to B and C talked to B and D talked to B, when E talks to A and C and D, system recommends to E to talk to B
    //todo find a ring of people who talked to each other one by one and suggest them to create a group
    //todo calculate the most and least popular person
    //todo calculate leaders (nodes with the most shortest paths to others) =? Most influential person
    //todo calculate group connectors
    //https://neo4j.com/blog/graph-algorithms-neo4j-15-different-graph-algorithms-and-what-they-do/
}
