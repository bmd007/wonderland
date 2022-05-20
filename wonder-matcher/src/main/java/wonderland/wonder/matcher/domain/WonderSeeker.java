package wonderland.wonder.matcher.domain;

import java.time.Instant;

//todo do we need seeker information like `name` here or we need to join with profiles table first?
public record WonderSeeker(String id, Location lastLocation, Instant updatedAt) {
    public static WonderSeeker empty(String id) {
        return new WonderSeeker(id, null, null);
    }

    public static WonderSeeker empty() {
        return new WonderSeeker(null, null, null);
    }

    public static WonderSeeker now(String id, double latitude, double longitude) {
        return new WonderSeeker(id, new Location(latitude, longitude), Instant.now());
    }
}
