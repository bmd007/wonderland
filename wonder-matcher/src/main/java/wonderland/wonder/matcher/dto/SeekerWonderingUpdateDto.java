package wonderland.wonder.matcher.dto;

import java.time.Instant;

public record SeekerWonderingUpdateDto(String wonderSeekerId, Instant timestamp, double latitude, double longitude) {
    public String activity() {
        return "bowling";
    }

    public String seekedWonder() {
        return "bowling";
    }
    public String getKey() {
        return wonderSeekerId;
    }

}
