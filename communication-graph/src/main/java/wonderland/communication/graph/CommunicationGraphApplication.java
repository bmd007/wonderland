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
import wonderland.communication.graph.domain.PersonInfluenceScoreProjection;
import wonderland.communication.graph.event.MessageSentEvent;
import wonderland.communication.graph.repository.CommunicationRepository;
import wonderland.communication.graph.repository.PersonRepository;

@EnableNeo4jRepositories("wonderland.*")
@RestController
@SpringBootApplication
public class CommunicationGraphApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationGraphApplication.class);
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private Neo4jClient client;
    @Autowired
    private CommunicationRepository communicationRepository;

    public static void main(String[] args) {
        SpringApplication.run(CommunicationGraphApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        personRepository.deleteAll();

        var receiver = personRepository.findByEmail("e")
                .orElseGet(() -> personRepository.save(Person.of("e")));
        var communication = Communication.toward(receiver);
        var sender = personRepository.findByEmail("pi").orElseGet(() -> Person.of("pi"));
        var toBeSavedSender = sender.addCommunication(communication);
        var savedSender = personRepository.save(toBeSavedSender);
        LOGGER.info("person {} saved", savedSender);

        var receiver3 = personRepository.findByEmail("e")
                .orElseGet(() -> personRepository.save(Person.of("e")));
        var communication3 = Communication.toward(receiver3);
        var sender3 = personRepository.findByEmail("pi").orElseGet(() -> Person.of("pi"));
        var toBeSavedSender3 = sender3.addCommunication(communication3);
        var savedSender3 = personRepository.save(toBeSavedSender3);
        LOGGER.info("person {} saved", savedSender3);

        var receiver2 = personRepository.findByEmail("pi")
                .orElseGet(() -> personRepository.save(Person.of("pi")));
        var communication2 = Communication.toward(receiver2);
        var sender2 = personRepository.findByEmail("pi").orElseGet(() -> Person.of("pi"));
        var toBeSavedSender2 = sender2.addCommunication(communication2);
        var savedSender2 = personRepository.save(toBeSavedSender2);
        LOGGER.info("person {} saved", savedSender2);

        var receiver4 = personRepository.findByEmail("e2")
                .orElseGet(() -> personRepository.save(Person.of("e2")));
        var communication4 = Communication.toward(receiver4);
        var sender4 = personRepository.findByEmail("pi").orElseThrow();
//        var sender4 = Person.of(personProjection);
//        var currentCommunications = communicationRepository.findAllByTo(sender4);
        var toBeSavedSender4 = sender4.addCommunication(communication4);
//                .addCommunications(currentCommunications);
        var savedSender4 = personRepository.save(toBeSavedSender4);
        LOGGER.info("person {} saved", savedSender4);

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
                .fetchAs(PersonInfluenceScoreProjection.class)
                .mappedBy((typeSystem, record) ->
                        new PersonInfluenceScoreProjection(record.get("email").asString(), record.get("score").asDouble()))
                .one()
                .ifPresent(System.out::println);
    }

    @KafkaListener(topicPattern = Topics.MESSAGES_EVENTS_TOPIC)
    public void messageEventsSentSubscription(@Payload MessageSentEvent messageSentEvent,
                                              @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        try {
            if (!messageSentEvent.from().equals(messageSentEvent.to())) {
                var receiver = personRepository.findByEmail(messageSentEvent.to())
                        .orElseGet(() -> personRepository.save(Person.of(messageSentEvent.to())));
                var communication = new Communication(null, receiver, messageSentEvent.time());
                var sender = personRepository.findByEmail(messageSentEvent.from())
                        .orElseGet(() -> Person.of(messageSentEvent.from()));
                var toBeSavedSender = sender.addCommunication(communication);
                var savedSender = personRepository.save(toBeSavedSender);
                LOGGER.info("person {} saved", savedSender);
            }
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
