package wonderland.wonder.matcher.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WonderSeekerLikedByHistory(
        String wonderSeekerName,
        Map<String, LocalDateTime> likedByHistory
) {
    public static WonderSeekerLikedByHistory empty(){
        return new WonderSeekerLikedByHistory(null, Map.of());
    }

    public static WonderSeekerLikedByHistory initialize(String wonderSeekerName){
        return new WonderSeekerLikedByHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikedByHistory withName(String wonderSeekerName){
        return new WonderSeekerLikedByHistory(wonderSeekerName, Map.of());
    }

    public WonderSeekerLikedByHistory addLikedByToHistory(String likeeName, LocalDateTime likedAt){
        Map<String, LocalDateTime> updatedLikeHistory = Stream.concat(likedByHistory.entrySet().stream(), Stream.of(Map.entry(likeeName, likedAt)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue,
                        (localDateTime, localDateTime2) -> localDateTime2));
        return new WonderSeekerLikedByHistory(wonderSeekerName, updatedLikeHistory);
    }

    @JsonIgnore
    public boolean isEmpty(){
        return wonderSeekerName==null || wonderSeekerName.isEmpty() || wonderSeekerName.isBlank();
    }

}
