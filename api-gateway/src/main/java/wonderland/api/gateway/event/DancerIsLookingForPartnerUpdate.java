package wonderland.api.gateway.event;

import wonderland.api.gateway.dto.Location;

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
