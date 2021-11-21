package wonderland.communication.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.neo4j.core.Neo4jClient;
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
import wonderland.communication.graph.dto.PersonInfluenceRankDto;
import wonderland.communication.graph.repository.PersonRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EnableNeo4jRepositories("wonderland.*")
@RestController
@SpringBootApplication
public class CommunicationGraphApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationGraphApplication.class);
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private Neo4jClient client;

    public static void main(String[] args) {
        SpringApplication.run(CommunicationGraphApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        personRepository.deleteAll();
        var p2 = Person.of("p2");
        personRepository.save(p2);
        var communication6 = new Communication(null, p2, Instant.now());
        var p22 = personRepository.findById("p2").get().addCommunication(communication6);
        personRepository.save(p22);
        client.query("""
                        CALL gds.pageRank.stream({
                            nodeProjection: 'Person',
                            relationshipProjection: 'SENT_MESSAGE_TO'
                        })
                        YIELD nodeId, score
                        MATCH (node) WHERE id(node) = nodeId
                        RETURN node.email AS email, score
                        ORDER BY score DESC
                        LIMIT 1""")
                .fetchAs(PersonInfluenceRankDto.class)
                .mappedBy((typeSystem, record) -> new PersonInfluenceRankDto(record.get("email").asString(),
                        record.get("score").asDouble()))
                .one()
                .ifPresent(System.out::println);
    }

    @KafkaListener(topicPattern = Topics.MESSAGES_EVENTS_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent,
                                              @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        try {
            var sender = personRepository.findById(messageSentEvent.from())
                    .orElseGet(() -> Person.of(messageSentEvent.from()));
            var receiver = personRepository.findById(messageSentEvent.to())
                    .orElseGet(() -> Person.of(messageSentEvent.to()));
            var communication = new Communication(null, receiver, messageSentEvent.time());
            var toBeSavedSender = sender.addCommunication(communication);
            var savedSender = personRepository.save(sender);
            LOGGER.info("person {} saved", toBeSavedSender);
//            LOGGER.info("communication {} saved", savedCommunication);
        } catch (Exception e) {
            LOGGER.error("Problem while processing {}", messageSentEvent, e);
        }
    }

    //todo recommendation: if A talked to B and C talked to B and D talked to B, when E talks to A and C and D, system recommends to E to talk to B
    //todo find a ring of people who talked to each other one by one and suggest them to create a group
    //todo calculate the most and least popular person
    //todo calculate leaders (nodes with the most shortest paths to others) =? Most influential person
    //todo calculate group connectors
    //https://neo4j.com/blog/graph-algorithms-neo4j-15-different-graph-algorithms-and-what-they-do/
}
