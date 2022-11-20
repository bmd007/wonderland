package wonderland.authentication.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TopicPublisher<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicPublisher.class);
    private static final int TIMEOUT_MILLIS = 1000;

    private final KafkaProducer<K, V> producer;
    private final String topic;
    private final Scheduler scheduler;

    public TopicPublisher(KafkaProducer<K, V> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
        scheduler = Schedulers.newSingle(topic);
    }

    public void publish(K key, V value) {
        var fut = producer.send(new ProducerRecord<>(topic, key, value));
        Mono.create(sink -> {
                    try {
                        fut.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                        sink.success();
                        LOGGER.debug("Message {} sent to {} topic successfully", value, topic);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        LOGGER.error("Failed to send: {} to {}", value, topic, e);
                        sink.error(e);
                    }
                })
                .subscribeOn(scheduler)
                .subscribe();
    }
}
