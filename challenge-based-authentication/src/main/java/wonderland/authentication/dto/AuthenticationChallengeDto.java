package wonderland.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import wonderland.authentication.domain.AuthenticationChallengeState;

import java.time.Instant;

public record AuthenticationChallengeDto(@NotBlank String signingNonce,
                                         @NotNull Instant expiresAt,
                                         @NotNull AuthenticationChallengeState state) {
}
