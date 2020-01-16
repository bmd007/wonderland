package wonderland.messenger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder(builderClassName = "MessageSentEventBuilder", toBuilder = true)
@JsonDeserialize(builder = MessageSentEvent.MessageSentEventBuilder.class)
public class MessageSentEvent {
    String from;
    String to;
    String body;
    Instant time;

    @JsonPOJOBuilder(withPrefix = "")
    public static class MessageSentEventBuilder {
    }

}
