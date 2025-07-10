package wonderland.authentication.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import wonderland.authentication.domain.AuthenticationChallenge;
import wonderland.authentication.dto.AuthenticationChallengeDto;
import wonderland.authentication.dto.AuthenticationChallengesDto;
import wonderland.authentication.dto.SignRequestDto;
import wonderland.authentication.event.internal.ChallengeCapturedEvent;
import wonderland.authentication.event.internal.ChallengeCreatedEvent;
import wonderland.authentication.event.internal.ChallengeSignedEvent;
import wonderland.authentication.event.internal.ChallengeUsedForLoginEvent;
import wonderland.authentication.event.internal.EventLogger;
import wonderland.authentication.service.AuthenticationChallengeViewService;
import wonderland.authentication.service.ViewService;

@RestController
@RequestMapping("/api/authentication/challenges")
public class AuthenticationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    private AuthenticationChallengeViewService authenticationChallengeViewService;
    private EventLogger eventLogger;

    public AuthenticationResource(EventLogger eventLogger, AuthenticationChallengeViewService authenticationChallengeViewService) {
        this.eventLogger = eventLogger;
        this.authenticationChallengeViewService = authenticationChallengeViewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthenticationChallenge> create() {
        AuthenticationChallenge newChallenge = AuthenticationChallenge.createNew();
        var event = new ChallengeCreatedEvent(newChallenge);
        eventLogger.log(event);
        return Mono.just(newChallenge);
    }

    @PutMapping("/{signingNonce}/capture")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void captureChallenge(@PathVariable String signingNonce) {
        var event = new ChallengeCapturedEvent(signingNonce);
        eventLogger.log(event);
    }

    @PutMapping("/{signingNonce}/sign")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void signChallenge(@PathVariable String signingNonce, @RequestBody SignRequestDto body) {
        var event = new ChallengeSignedEvent(signingNonce, body.jwt());
        eventLogger.log(event);
    }

    @PutMapping("/{signingNonce}/login/{loginNonce}")
    public void login(@PathVariable String signingNonce, @PathVariable String loginNonce) {
        //todo get the challenge and check it's state
        var event = new ChallengeUsedForLoginEvent(signingNonce);
        eventLogger.log(event);
    }

    //isHighLevelQuery query param is related to inter instance communication and it should be true in normal operations or not defined
    @GetMapping
    public Mono<AuthenticationChallengesDto> getChallenges(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return authenticationChallengeViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/{signingNonce}")
    public Mono<AuthenticationChallengeDto> getChallenge(@PathVariable String signingNonce) {
        return authenticationChallengeViewService.getById(signingNonce)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge Not found: " + signingNonce)));
    }
}
