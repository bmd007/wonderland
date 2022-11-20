package wonderland.wonder.matcher.event;

import jakarta.validation.constraints.NotBlank;

public record DancePartnerSeekerHasLikedAnotherDancerEvent(@NotBlank String liker,
                                                           @NotBlank String likee) implements Event {

    @Override
    public String key() {
        return liker;
    }
}
