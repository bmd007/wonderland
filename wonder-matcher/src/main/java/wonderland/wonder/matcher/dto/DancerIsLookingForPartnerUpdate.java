package wonderland.wonder.matcher.dto;

import wonderland.wonder.matcher.domain.Location;

import javax.validation.constraints.NotBlank;

public record DancerIsLookingForPartnerUpdate(
        @NotBlank String dancerName,
        Location location

) implements Event {

    @Override
    public String key() {
        return dancerName;
    }
}
