package wonderland.wonder.matcher.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WonderSeekerMatchHistory(String wonderSeekerName, Map<String, LocalDateTime> matchHistory) {
    public static WonderSeekerMatchHistory empty() {
        return new WonderSeekerMatchHistory(null, Map.of());
    }

    public static WonderSeekerMatchHistory initialize(String wonderSeekerName) {
        return new WonderSeekerMatchHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerMatchHistory withName(String wonderSeekerName) {
        return new WonderSeekerMatchHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerMatchHistory addLikeToHistory(String likeeName, LocalDateTime likedAt) {
        Map<String, LocalDateTime> updatedLikeHistory = Stream.concat(matchHistory.entrySet().stream(), Stream.of(Map.entry(likeeName, likedAt)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue,
                        (localDateTime, localDateTime2) -> localDateTime2));
        return new WonderSeekerMatchHistory(wonderSeekerName, updatedLikeHistory);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return wonderSeekerName == null || wonderSeekerName.isEmpty() || wonderSeekerName.isBlank();
    }

}
