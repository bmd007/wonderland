package wonderland.communication.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.neo4j.core.Neo4jTemplate;
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
import wonderland.communication.graph.repository.PersonRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EnableNeo4jRepositories("wonderland.*")
@RestController
@SpringBootApplication
public class CommunicationGraphApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationGraphApplication.class);
    @Autowired private PersonRepository personRepository;

    public static void main(String[] args) {
        SpringApplication.run(CommunicationGraphApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        personRepository.deleteAll();
        var p1 = new Person(null, "p1", new ArrayList<>());
        var p2 = new Person(null, "p2", new ArrayList<>());
        var p3 = new Person(null, "p3", new ArrayList<>());
        var p4 = new Person(null, "p4", new ArrayList<>());
        var p5 = new Person(null, "p5", new ArrayList<>());
        var p6 = new Person(null, "p6", new ArrayList<>());
        var communication = new Communication(null, p2, Instant.now());
        var communication2 = new Communication(null, p3, Instant.now());
        var communication3 = new Communication(null, p4, Instant.now());
        var communication4 = new Communication(null, p5, Instant.now());
        var communication5 = new Communication(null, p6, Instant.now());
        p1.addCommunication(communication);
        p1.addCommunication(communication2);
        p1.addCommunication(communication3);
        p1.addCommunication(communication4);
        p1.addCommunication(communication5);
        personRepository.saveAll(List.of(p2, p3, p4, p5, p6));
        personRepository.save(p1);

        var communication6 = new Communication(null, p1, Instant.now());
        p2.addCommunication(communication6);
        p4.addCommunication(communication6);
        personRepository.save(p2);
        personRepository.save(p4);

        var communication7 = new Communication(null, p2, Instant.now());
        p4.addCommunication(communication7);
        personRepository.save(p4);
    }

    @KafkaListener(topicPattern = Topics.MESSAGES_EVENTS_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent,
                                              @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        try {

//            LOGGER.info("communication {} saved", savedCommunication);
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
