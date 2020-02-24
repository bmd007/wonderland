package wonderland.message.counter;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import wonderland.message.counter.config.Topics;
import wonderland.message.counter.dto.MessageCounterDto;
import wonderland.message.counter.event.external.MessageSentEvent;
import wonderland.message.counter.event.internal.CounterIncreasedEvent;
import wonderland.message.counter.event.internal.CounterRestartedEvent;
import wonderland.message.counter.event.internal.Event;
import wonderland.message.counter.serialization.JsonDeserializer;
import wonderland.message.counter.util.EmbeddedKafkaHelper;
import wonderland.message.counter.util.KafkaStreamsAwait;
import wonderland.message.counter.util.TopicPublisher;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith({SpringExtension.class})
@SpringBootTest(properties = {
        "server_port=4965",
        "kafka.streams.server.config.app-port=4965",
        "kafka.streams.server.config.app-ip=localhost"
})
@EmbeddedKafka(partitions = 1, topics = {Topics.EVENT_LOG})
@DirtiesContext
@EnableKafkaStreams
@AutoConfigureWebTestClient
public class AcceptanceTest {

    private static final int TIMEOUT = 10_000;

    @Autowired
    WebTestClient client;

    @Autowired
    KafkaStreamsAwait await;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;
    EmbeddedKafkaHelper<String, Event> kafkaHelper;

    @Autowired
    @Qualifier("messageSentEventKafkaProducer")
    KafkaProducer<String, MessageSentEvent> messageSentEventProducer;

    @BeforeEach
    void setupKafka() throws Exception {
        await.await();
        kafkaHelper = new EmbeddedKafkaHelper<>(embeddedKafka, Topics.EVENT_LOG, StringDeserializer.class, EventDeserializer.class);
    }

    @AfterEach
    void tearDown() {
        kafkaHelper.tearDown();
    }

    @Test
    public void sendAMessageAndThenCheckCounterForTheSender() throws InterruptedException, ExecutionException {
        // given
        client.post()
                .uri("/api/counter/message/mahdi/restart")
                .exchange()
                .expectStatus()
                .isCreated();

        var record = kafkaHelper.getRecords().poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertNotNull(record);
        assertTrue(record.value() instanceof CounterRestartedEvent);

//        //faking a message sent
        messageSentEventProducer.send(new ProducerRecord<>(Topics.MESSAGE_EVENT_TOPIC, "mahdi", MessageSentEvent.builder()
                .withBody("hhhhhheeee")
                .withFrom("mahdi")
                .withTo("LOVE")
                .withTime(Instant.now())
                .build())).get();

        var record2 = kafkaHelper.getRecords().poll(TIMEOUT, TimeUnit.MILLISECONDS);
        assertNotNull(record2);
        assertTrue(record2.value() instanceof CounterIncreasedEvent);

        Thread.sleep(100); // Eventual consistency :))
        //when
        client.get()
                .uri("/api/counter/message/sent/from/mahdi")
                .exchange()
                //then
                .expectStatus()
                .isOk()
                .expectBody(MessageCounterDto.class)
                .value(counterDto -> assertEquals(1, counterDto.getNumberOfSentMessages()));
    }

    public static class EventDeserializer extends JsonDeserializer<Event> {
        public EventDeserializer() { super(Event.class); }
    }

}
