package wonderland.authentication.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@JsonDeserialize(builder = AuthenticationChallenge.AuthenticationChallengeBuilder.class)
@Builder(toBuilder = true)
@Data
public final class AuthenticationChallenge {

    private final @NotBlank String signingNonce;
    private final @NotBlank String loginNonce;
    private final @NotNull Instant expiresAt;
    private final @NotNull States state;

    public AuthenticationChallenge(String signingNonce,
                                   String loginNonce,
                                   Instant expiresAt,
                                   @NotNull AuthenticationChallenge.States state) {
        this.signingNonce = signingNonce;
        this.loginNonce = loginNonce;
        this.expiresAt = expiresAt;
        this.state = state;
    }

    public static AuthenticationChallenge createNew() {
        return AuthenticationChallenge.builder()
                .signingNonce(UUID.randomUUID().toString())
                .loginNonce(UUID.randomUUID().toString())
                .state(States.AWAITING_CAPTURE)
                .expiresAt(Instant.now().plusSeconds(30))
                .build();
    }

    public AuthenticationChallenge capture() {
        if (this.getState().equals(States.AWAITING_CAPTURE)) {
            return toBuilder()
                    .state(States.CAPTURED)
                    .expiresAt(Instant.now().plusSeconds(300))
                    .build();
        }
        throw new IllegalStateException("wrong state transition is requested. Only AWAITING_CAPTURE challenges can be CAPTURED");
    }

    public AuthenticationChallenge sign() {
        if (this.getState().equals(States.CAPTURED) || this.getState().equals(States.AWAITING_CAPTURE)) {
            return toBuilder()
                    .state(States.SIGNED)
                    .expiresAt(Instant.now().plusSeconds(100))
                    .build();
        }
        throw new IllegalStateException("wrong state transition is requested. Only CAPTURED or AWAITING_CAPTURE challenges can be signed");
    }

    public AuthenticationChallenge invalidate() {
        return toBuilder().state(States.INVALID).expiresAt(Instant.EPOCH).build();
    }

    public @NotBlank String loginNonce() {
        return loginNonce;
    }

    public @NotBlank String signingNonce() {
        return signingNonce;
    }

    public @NotNull Instant expiresAt() {
        return expiresAt;
    }

    public @NotNull States state() {
        return state;
    }

    public enum States {
        AWAITING_CAPTURE, CAPTURED, SIGNED, INVALID
    }
}