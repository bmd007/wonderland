package wonderland.wonder.matcher.event;

import javax.validation.constraints.NotBlank;

public record WonderSeekersMatchedEvent(@NotBlank String matchee1, @NotBlank String matchee2) implements Event {

    @Override
    public String key() {
        return matchee1;
    }

    public WonderSeekersMatchedEvent reverse(){
        return new WonderSeekersMatchedEvent(this.matchee2, this.matchee1);
    }
}
