package wonderland.message.counter.event.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.MINIMAL_CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "event")
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Event {

    String getKey();

}
