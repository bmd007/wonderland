package wonderland.wonder.matcher.serialization;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return JsonMapperProvider.MAPPER.writeValueAsBytes(data);
        } catch (IOException e) {
            LOGGER.error("Cannot serialize {}", data);
            e.printStackTrace();
            return null;
        }
    }
}
