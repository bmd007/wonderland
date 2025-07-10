package wonderland.authentication.event.internal;

public record ChallengeCapturedEvent(String signingNonce) implements Event {
    @Override
    public String getKey() {
        return signingNonce;
    }
}
