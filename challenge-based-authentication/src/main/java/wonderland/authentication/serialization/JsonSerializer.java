package wonderland.authentication.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);
    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return MAPPER.writeValueAsBytes(data);
        } catch (IOException e) {
            LOGGER.error("Cannot serialize {}", data);
            return null;
        }
    }
}
