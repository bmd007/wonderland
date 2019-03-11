package test;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class MySerde implements Serde<PageViewEvent> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<PageViewEvent> serializer() {
        Map<String, Object> serdeProps = new HashMap<>();

        Serializer<PageViewEvent> pageViewSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", PageViewEvent.class);
        pageViewSerializer.configure(serdeProps, false);

        return pageViewSerializer;
    }

    @Override
    public Deserializer<PageViewEvent> deserializer() {
        Map<String, Object> serdeProps = new HashMap<>();

        Deserializer<PageViewEvent> pageViewDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", PageViewEvent.class);
        pageViewDeserializer.configure(serdeProps, false);

        return pageViewDeserializer;
    }
}
