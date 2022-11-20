package wonderland.authentication.dto;

import wonderland.authentication.domain.AuthenticationChallengeState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AuthenticationChallengeDto(@NotBlank String signingNonce,
                                         @NotNull Instant expiresAt,
                                         @NotNull AuthenticationChallengeState state) {
}
