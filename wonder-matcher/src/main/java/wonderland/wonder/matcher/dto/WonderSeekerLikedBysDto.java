package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WonderSeekerLikedBysDto(
        String wonderSeekerName,
        Map<String, LocalDateTime> likedByHistory
) {
    public static WonderSeekerLikedBysDto empty() {
        return new WonderSeekerLikedBysDto(null, Map.of());
    }

    public static WonderSeekerLikedBysDto initialize(String wonderSeekerName) {
        return new WonderSeekerLikedBysDto(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikedBysDto withName(String wonderSeekerName) {
        return new WonderSeekerLikedBysDto(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikedBysDto addLikeToHistory(String likeeName, LocalDateTime likedAt) {
        Map<String, LocalDateTime> updatedLikeHistory = Stream.concat(likedByHistory.entrySet().stream(), Stream.of(Map.entry(likeeName, likedAt)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue,
                        (localDateTime, localDateTime2) -> localDateTime2));
        return new WonderSeekerLikedBysDto(wonderSeekerName, updatedLikeHistory);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return wonderSeekerName == null || wonderSeekerName.isEmpty() || wonderSeekerName.isBlank();
    }

}
