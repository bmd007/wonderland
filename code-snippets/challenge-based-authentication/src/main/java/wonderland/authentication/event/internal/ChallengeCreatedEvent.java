package wonderland.authentication.event.internal;

import wonderland.authentication.domain.AuthenticationChallenge;

public record ChallengeCreatedEvent(AuthenticationChallenge authenticationChallenge) implements Event {
    @Override
    public String getKey() {
        return authenticationChallenge.getSigningNonce();
    }
}
