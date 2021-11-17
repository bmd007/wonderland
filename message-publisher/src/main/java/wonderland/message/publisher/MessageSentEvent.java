package wonderland.message.publisher;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public final class MessageSentEvent {
    private final String from;
    private final String to;
    private final String body;
    private final Instant time;
}
