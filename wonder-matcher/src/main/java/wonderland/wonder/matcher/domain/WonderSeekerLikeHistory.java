package wonderland.wonder.matcher.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WonderSeekerLikeHistory(String wonderSeekerName, Map<String, LocalDateTime> likeHistory) {
    public static WonderSeekerLikeHistory empty() {
        return new WonderSeekerLikeHistory(null, Map.of());
    }

    public static WonderSeekerLikeHistory initialize(String wonderSeekerName) {
        return new WonderSeekerLikeHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikeHistory withName(String wonderSeekerName) {
        return new WonderSeekerLikeHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikeHistory addLikeToHistory(String likeeName, LocalDateTime likedAt) {
        Map<String, LocalDateTime> updatedLikeHistory = Stream.concat(likeHistory.entrySet().stream(), Stream.of(Map.entry(likeeName, likedAt)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue,
                        (localDateTime, localDateTime2) -> localDateTime2));
        return new WonderSeekerLikeHistory(wonderSeekerName, updatedLikeHistory);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return wonderSeekerName == null || wonderSeekerName.isEmpty() || wonderSeekerName.isBlank();
    }

}
