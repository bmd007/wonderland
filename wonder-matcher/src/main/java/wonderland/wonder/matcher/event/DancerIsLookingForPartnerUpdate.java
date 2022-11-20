package wonderland.wonder.matcher.event;

import jakarta.validation.constraints.NotBlank;
import wonderland.wonder.matcher.domain.Location;

public record DancerIsLookingForPartnerUpdate(
        @NotBlank String dancerName,
        Location location) implements Event {
    @Override
    public String key() {
        return dancerName;
    }
}
