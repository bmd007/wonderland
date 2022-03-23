package wonderland.authentication.dto;

import wonderland.authentication.domain.AuthenticationChallenge;

import java.util.List;

public record AuthenticationChallengesDto(List<AuthenticationChallenge> authenticationChallenges) {
}
