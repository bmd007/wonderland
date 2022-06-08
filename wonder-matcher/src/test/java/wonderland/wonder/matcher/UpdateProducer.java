package wonderland.wonder.matcher;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.domain.Location;
import wonderland.wonder.matcher.dto.DancerIsLookingForPartnerUpdate;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static wonderland.wonder.matcher.serialization.CustomSerdes.DANCER_SEEKING_PARTNER_JSON_SERDE;

@Component
public class UpdateProducer {

    private final KafkaProducer<String, DancerIsLookingForPartnerUpdate> positionUpdateProducer;

    public UpdateProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        var providerConfig = new Properties();
        providerConfig.put("bootstrap.servers", bootstrapServers);
        positionUpdateProducer = new KafkaProducer<>(providerConfig, new StringSerializer(), DANCER_SEEKING_PARTNER_JSON_SERDE.serializer());
    }

    public void producePositionUpdate(String key, double latitude, double longitude) {
        try {
            var location = new Location(latitude, longitude);
            var value = new DancerIsLookingForPartnerUpdate(key, location);
            var record = new ProducerRecord<>(Topics.WONDER_SEEK_UPDATES_TOPIC, key, value);
            positionUpdateProducer.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
