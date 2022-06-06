package wonderland.api.gateway.dto;

import javax.validation.constraints.NotBlank;

public record DancePartnerSeekerIsDisLikedEvent(@NotBlank String whoHasDisLiked, @NotBlank String whomIsDisLiked) implements DancePartnerEvent {
    @Override
    public String dancerName() {
        return whoHasDisLiked;
    }

    @Override
    public String key() {
        return dancerName();
    }
}
