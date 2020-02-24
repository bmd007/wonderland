package wonderland.message.counter.event.internal;

import wonderland.message.counter.config.Topics;
import wonderland.message.counter.util.TopicPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EventLogger {

    private TopicPublisher<String, Event> publisher;

    public EventLogger(@Qualifier("eventLogKafkaProducer") KafkaProducer<String, Event> eventProducer) {
        publisher = new TopicPublisher<>(eventProducer, Topics.EVENT_LOG);
    }

    public void log(Event event) {
        publisher.publish(event.getKey(), event);
    }
}
