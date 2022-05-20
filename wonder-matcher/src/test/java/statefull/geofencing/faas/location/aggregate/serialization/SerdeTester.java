package statefull.geofencing.faas.location.aggregate.serialization;


import org.apache.kafka.common.serialization.Serde;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerdeTester<T> {

    private final Serde<T> serde;

    public SerdeTester(Serde<T> serde) {
        this.serde = serde;
    }

    public void serializeAndDeserializeShouldBeEqual(T object) {
        var bytes = serde.serializer().serialize("topic", object);
        var deserialized = serde.deserializer().deserialize("topic", bytes);
        assertEquals(object, deserialized);
        assertEquals(object.hashCode(), deserialized.hashCode());
        assertEquals(object.toString(), deserialized.toString());
    }

}
