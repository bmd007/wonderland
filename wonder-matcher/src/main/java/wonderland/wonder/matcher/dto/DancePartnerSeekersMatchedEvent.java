package wonderland.wonder.matcher.dto;

import javax.validation.constraints.NotBlank;

public record DancePartnerSeekersMatchedEvent(@NotBlank String matchee1, @NotBlank String matchee2) implements Event {

    @Override
    public String key() {
        return matchee1;
    }

    public DancePartnerSeekersMatchedEvent reverse(){
        return new DancePartnerSeekersMatchedEvent(this.matchee2, this.matchee1);
    }
}
