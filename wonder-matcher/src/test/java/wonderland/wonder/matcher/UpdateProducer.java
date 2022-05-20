package wonderland.wonder.matcher;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.domain.Location;
import wonderland.wonder.matcher.dto.SeekerWonderingUpdateDto;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class UpdateProducer {

    private final KafkaProducer<String, SeekerWonderingUpdateDto> positionUpdateProducer;

    public UpdateProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        var providerConfig = new Properties();
        providerConfig.put("bootstrap.servers", bootstrapServers);
        positionUpdateProducer = new KafkaProducer<>(providerConfig, new StringSerializer(), CustomSerdes.WONDER_SEEKER_DTO_JSON_SERDE.serializer());
    }

    public void producePositionUpdate(String key, double latitude, double longitude) {
        try {
            var location = new Location(latitude, longitude);
            var value = new SeekerWonderingUpdateDto(key, location);
            var record = new ProducerRecord<>(Topics.WONDER_SEEK_UPDATES_TOPIC, key, value);
            positionUpdateProducer.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
