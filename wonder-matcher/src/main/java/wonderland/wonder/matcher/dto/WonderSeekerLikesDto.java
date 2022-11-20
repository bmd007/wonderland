package wonderland.wonder.matcher.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record WonderSeekerLikesDto(
        String wonderSeekerName,
        Map<String, LocalDateTime> likeHistory
) {

}
