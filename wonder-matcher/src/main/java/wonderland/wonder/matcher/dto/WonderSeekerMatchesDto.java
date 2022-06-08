package wonderland.wonder.matcher.dto;

import java.time.LocalDateTime;
import java.util.Map;
public record WonderSeekerMatchesDto(
        String wonderSeekerName,
        Map<String, LocalDateTime> matchHistory
) {
    public boolean isEmpty(){
        return wonderSeekerName==null || wonderSeekerName.isEmpty() || wonderSeekerName.isBlank();
    }
}
