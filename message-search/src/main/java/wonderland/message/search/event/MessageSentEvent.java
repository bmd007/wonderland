package wonderland.message.search.event;

import java.time.Instant;

public record MessageSentEvent(String from, String to, String body, Instant time) {
}
