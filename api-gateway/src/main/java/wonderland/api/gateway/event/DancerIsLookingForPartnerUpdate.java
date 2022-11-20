package wonderland.api.gateway.event;

import jakarta.validation.constraints.NotBlank;
import wonderland.api.gateway.dto.Location;

public record DancerIsLookingForPartnerUpdate(
        @NotBlank String dancerName,
        Location location

) implements Event {

    @Override
    public String key() {
        return dancerName;
    }
}
