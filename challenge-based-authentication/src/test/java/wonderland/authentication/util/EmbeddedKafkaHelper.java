package wonderland.authentication.util;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to use embedded kafka. Usage:
 *
 * <pre>
 * <code>
    &#64;BeforeEach
    void setup(ApplicationContext context) {
        helper = new EmbeddedKafkaHelper<>(embeddedKafka, Topics.ORDER_COMMANDS,
                LongDeserializer.class, OrderCommandDeserializer.class);
    }
    &#64;AfterEach
    void tearDown() {
        helper.tearDown();
    }
 * </code>
 * </pre>
 *
 * <br>
 * This saves the coder from repeating initialization code and does a clean tear down (see:
 * https://github.com/spring-projects/spring-kafka/issues/194)
 *
 * @param <K>
 *            deserializer class for the keys
 * @param <V>
 *            deserializer class for the values
 */
public class EmbeddedKafkaHelper<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedKafkaHelper.class);

    private EmbeddedKafkaBroker embeddedKafka;
    private KafkaMessageListenerContainer<K, V> container;
    private String topic;
    private BlockingQueue<ConsumerRecord<K, V>> records = new LinkedBlockingQueue<>();

    public EmbeddedKafkaHelper(EmbeddedKafkaBroker embeddedKafka, String topic,
                               Class<? extends Deserializer<K>> keyDeserializer, Class<? extends Deserializer<V>> valueDeserializer) {
        this.embeddedKafka = embeddedKafka;
        this.topic = topic;
        startContainer(keyDeserializer, valueDeserializer);
    }

    private void startContainer(Class<? extends Deserializer<K>> keyDeserializer,
                                Class<? extends Deserializer<V>> valueDeserializer) {
        var consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        var factory = new DefaultKafkaConsumerFactory<K, V>(consumerProps);
        var containerProperties = new ContainerProperties(topic);
        container = new KafkaMessageListenerContainer<>(factory, containerProperties);
        container.setupMessageListener((MessageListener<K, V>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    public void tearDown() {
        try {
            ConsumerRecord<K, V> r = null;
            while ((r = records.poll(100, TimeUnit.MILLISECONDS)) != null) {
                LOGGER.info("Discarding unconsumed message: {}->{}", r.key(), r.value());
            }
            container.stop();
        } catch (Exception e) {
        }
        // embeddedKafka.getKafkaServers().forEach(KafkaServer::shutdown);
        // embeddedKafka.getKafkaServers().forEach(KafkaServer::awaitShutdown);
    }

    public KafkaMessageListenerContainer<K, V> getContainer() {
        return container;
    }

    public BlockingQueue<ConsumerRecord<K, V>> getRecords() {
        return records;
    }
}
