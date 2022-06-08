package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
public record WonderSeekerMatchesDto(
        String wonderSeekerName,
        Map<String, LocalDateTime> matchHistory
) {
}
