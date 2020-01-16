package wonderland.messenger;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MessageSentEvent {
    String from;
    String to;
    String body;
    Instant time;
}
