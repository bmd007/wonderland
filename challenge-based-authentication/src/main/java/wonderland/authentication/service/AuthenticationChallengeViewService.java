package wonderland.authentication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import wonderland.authentication.config.Stores;
import wonderland.authentication.domain.AuthenticationChallenge;
import wonderland.authentication.dto.AuthenticationChallengeDto;
import wonderland.authentication.dto.AuthenticationChallengesDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


@Service
public class AuthenticationChallengeViewService extends ViewService<AuthenticationChallengesDto, AuthenticationChallengeDto, AuthenticationChallenge> {

    final static Function<AuthenticationChallengesDto, List<AuthenticationChallengeDto>> LIST_EXTRACTOR = AuthenticationChallengesDto::authenticationChallenges;
    final static Function<List<AuthenticationChallengeDto>, AuthenticationChallengesDto> LIST_WRAPPER = AuthenticationChallengesDto::new;
    final static BiFunction<String, AuthenticationChallenge, AuthenticationChallengeDto> DTO_MAPPER =
            (key, authenticationChallenge) -> new AuthenticationChallengeDto(key,
                    authenticationChallenge.expiresAt(),
                    authenticationChallenge.state());

    public AuthenticationChallengeViewService(StreamsBuilderFactoryBean streams,
                                              @Value("${kafka.streams.server.config.app-ip}") String ip,
                                              @Value("${kafka.streams.server.config.app-port}") int port,
                                              ViewResourcesClient commonClient) {
        super(ip, port, streams, Stores.CHALLENGE_STATE_STORE,
                AuthenticationChallengesDto.class, AuthenticationChallengeDto.class, DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "api/authentication/challenges", commonClient);
    }
}
