package wonderland.wonder.matcher.event;

import wonderland.wonder.matcher.domain.Location;

import jakarta.validation.constraints.NotBlank;

public record DancerIsLookingForPartnerUpdate(
        @NotBlank String dancerName,
        Location location

) implements Event {

    @Override
    public String key() {
        return dancerName;
    }
}
