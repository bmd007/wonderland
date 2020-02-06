package wonderland.message.counter.config;

import wonderland.message.counter.event.internal.Event;
import wonderland.message.counter.serialization.JsonSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaProducersConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    @Qualifier("eventLogKafkaProducer")
    public KafkaProducer<String, Event> eventProducer() {
        var producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", bootstrapServers);
        producerConfig.put("enable.idempotence", "true"); //this provide foundation for transaction in the boundaries of this application through kafka.
        return new KafkaProducer(producerConfig, new StringSerializer(), new JsonSerializer<Event>());
    }

}
