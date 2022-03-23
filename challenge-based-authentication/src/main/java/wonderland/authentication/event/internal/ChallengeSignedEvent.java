package wonderland.authentication.event.internal;

public record ChallengeSignedEvent(String signingNonce, String jwt) implements Event {
    @Override
    public String getKey() {
        return signingNonce;
    }
}
