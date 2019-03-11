package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonPOJOSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Default constructor needed by Kafka
     */

    int i=0;

    private Class<T> tClass;

    public JsonPOJOSerializer() {
    }

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        tClass = (Class<T>) props.get("JsonPOJOClass");
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null)
            return null;

        i++;

        try {
            byte[] bytes = objectMapper.writer().forType(tClass).writeValueAsBytes(data);
            var Sered = new String(bytes);
            System.out.println("BMD:SER::"+Sered+"::"+i);

            return bytes;
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {
    }

}