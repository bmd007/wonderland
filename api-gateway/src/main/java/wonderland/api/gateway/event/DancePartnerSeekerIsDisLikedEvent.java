package wonderland.api.gateway.event;

import jakarta.validation.constraints.NotBlank;

public record DancePartnerSeekerIsDisLikedEvent(@NotBlank String disLiker, @NotBlank String disLikee) implements Event {
    @Override
    public String key() {
        return disLiker;
    }
}
