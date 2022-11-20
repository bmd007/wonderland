package wonderland.wonder.matcher.event;

import java.time.LocalDateTime;

public interface Event {
    String key();

    default LocalDateTime eventTime() {
        return LocalDateTime.now();
    }
}
