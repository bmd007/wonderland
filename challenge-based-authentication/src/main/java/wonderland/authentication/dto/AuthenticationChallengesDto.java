package wonderland.authentication.dto;

import java.util.List;

public record AuthenticationChallengesDto(List<AuthenticationChallengeDto> authenticationChallenges) {
}
