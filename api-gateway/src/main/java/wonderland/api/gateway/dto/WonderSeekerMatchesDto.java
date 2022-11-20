package wonderland.api.gateway.dto;


import java.time.LocalDateTime;
import java.util.Map;

public record WonderSeekerMatchesDto(String wonderSeekerName, Map<String, LocalDateTime> matchHistory) {
}
