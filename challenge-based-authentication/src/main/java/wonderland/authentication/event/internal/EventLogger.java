package wonderland.authentication.event.internal;

import wonderland.authentication.config.Topics;
import wonderland.authentication.util.TopicPublisher;
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
