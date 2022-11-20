package wonderland.message.publisher;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MessageSentEvent {
    String from;
    String to;
    String body;
    Instant time;
}
