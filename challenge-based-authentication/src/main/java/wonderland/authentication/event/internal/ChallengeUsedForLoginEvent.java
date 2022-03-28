package wonderland.authentication.event.internal;

public record ChallengeUsedForLoginEvent(String signingNonce) implements Event {
    @Override
    public String getKey() {
        return signingNonce;
    }
}
