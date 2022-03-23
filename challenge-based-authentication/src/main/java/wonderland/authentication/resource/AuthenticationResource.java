package wonderland.authentication.resource;

import org.bouncycastle.asn1.cmp.Challenge;
import org.springframework.http.HttpStatus;
import wonderland.authentication.domain.AuthenticationChallenge;
import wonderland.authentication.dto.SignRequestDto;
import wonderland.authentication.event.internal.ChallengeCreatedEvent;
import wonderland.authentication.exception.NotFoundException;
import wonderland.authentication.event.internal.EventLogger;
import wonderland.authentication.service.MessageCounterViewService;
import wonderland.authentication.service.ViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/authentication/challenges")
public class AuthenticationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    private MessageCounterViewService messageCounterViewService;
    private EventLogger eventLogger;

    public AuthenticationResource(EventLogger eventLogger, MessageCounterViewService messageCounterViewService) {
        this.eventLogger = eventLogger;
        this.messageCounterViewService = messageCounterViewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationChallenge create() {
        AuthenticationChallenge newChallenge = AuthenticationChallenge.createNew();
        var event = new ChallengeCreatedEvent(newChallenge);
        eventLogger.log(event);
    }

    @PostMapping("/{signingNonce}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void captureChallenge(@PathVariable String signingNonce) {
        var event = new ChallengeCapturedEvent(signingNonce);
        eventLogger.log(event);
    }

    @PostMapping("/{signingNonce}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void signChallenge(@PathVariable String signingNonce, SignRequestDto body) {
        var event = new ChallengeCapturedEvent(signingNonce, body.jwt());
        eventLogger.log(event);
    }

    @PostMapping("/{signingNonce}/{loginNonce}")
    public void login(@PathVariable String signingNonce,@PathVariable String loginNonce) {
        //todo get the challenge and check it's state
        var event = new ChallengeUsedForLoginEvent(signingNonce);
        eventLogger.log(event);
    }

    //isHighLevelQuery query param is related to inter instance communication and it should be true in normal operations or not defined
    @GetMapping
    public Mono<MessageCountersDto> getCounters(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return messageCounterViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/{signingNonce}")
    public Mono<Integer> getCountersSum(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return messageCounterViewService.getAll(isHighLevelQuery)
                .flatMapIterable(messageCountersDto -> messageCountersDto.getMessageCounters())
                .map(messageCounterDto -> messageCounterDto.getNumberOfSentMessages())
                .reduce((integer, integer2) -> integer + integer2);
    }

//    @GetMapping("/sent/from/{sender}")
//    public Mono<MessageCounterDto> getCounterByName(@PathVariable("sender") String sender) {
//        return messageCounterViewService.getById(sender)
//                .switchIfEmpty(Mono.error(new NotFoundException(String.format("%s not found (%s doesn't exist). Maybe has not sent any messages yet", "Sender", sender))));
//    }
}
