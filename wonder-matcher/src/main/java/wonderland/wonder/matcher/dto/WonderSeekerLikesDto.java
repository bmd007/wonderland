package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WonderSeekerLikesDto(
        String wonderSeekerName,
        Map<String, LocalDateTime> likeHistory
) {

}
