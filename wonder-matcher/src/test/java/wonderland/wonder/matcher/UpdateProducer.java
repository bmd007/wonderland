package wonderland.wonder.matcher;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import statefull.geofencing.faas.common.dto.WonderSeekerLocationUpdate;
import wonderland.wonder.matcher.config.Topics;
import wonderland.wonder.matcher.serialization.CustomSerdes;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Component
public class UpdateProducer {

    private final KafkaProducer<String, WonderSeekerLocationUpdate> positionUpdateProducer;

    public UpdateProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        var providerConfig = new Properties();
        providerConfig.put("bootstrap.servers", bootstrapServers);
        positionUpdateProducer = new KafkaProducer<>(providerConfig, new StringSerializer(), CustomSerdes.WONDER_SEEKER_DTO_JSON_SERDE.serializer());
    }

    public void producePositionUpdate(String key, double latitude, double longitude) {
        try {
            var value = WonderSeekerLocationUpdate.newBuilder()
                    .withLatitude(latitude)
                    .withLongitude(longitude)
                    .withWonderSeekerId(key)
                    .withTimestamp(Instant.now())
                    .build();
            var record = new ProducerRecord<>(Topics.WONDER_SEEKER_LOCATION_UPDATE_TOPIC, key, value);
            positionUpdateProducer.send(record).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
