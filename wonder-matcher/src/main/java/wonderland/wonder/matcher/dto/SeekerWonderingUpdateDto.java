package wonderland.wonder.matcher.dto;

import java.time.LocalDateTime;

public record SeekerWonderingUpdateDto(String wonderSeekerId, LocalDateTime eventTime, double latitude, double longitude) {
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
