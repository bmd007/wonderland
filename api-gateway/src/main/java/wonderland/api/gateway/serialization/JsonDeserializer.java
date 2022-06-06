package wonderland.api.gateway.serialization;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonDeserializer<T> implements Deserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDeserializer.class);
    private final Class<T> clazz;

    public JsonDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return JsonMapperProvider.MAPPER.readValue(data, clazz);
        } catch (IOException e) {
            LOGGER.error("Failed to deserialize message of class {}", clazz.getName(), e);
            e.printStackTrace();
            return null;
        }
    }
}
