package wonderland.api.gateway.dto;

import javax.validation.constraints.NotBlank;

public record DancePartnerSeekerIsLikedEvent(@NotBlank String whoHasLiked, @NotBlank String whomIsLiked) implements DancePartnerEvent {
    @Override
    public String dancerName() {
        return whoHasLiked;
    }

    @Override
    public String key() {
        return dancerName();
    }
}
