package wonderland.authentication.event.internal;

import org.apache.kafka.streams.kstream.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import wonderland.authentication.domain.AuthenticationChallenge;

@Component
public class EventHandler implements Aggregator<String, Event, AuthenticationChallenge> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    @Override
    public AuthenticationChallenge apply(String key, Event event, AuthenticationChallenge currentValue) {
        //todo check for expiration and return null if failed
        //return null if current state is invalidated
        if (event instanceof ChallengeCreatedEvent challengeCreatedEvent) {
            return challengeCreatedEvent.authenticationChallenge();
        } else if (event instanceof ChallengeCapturedEvent) {
            return currentValue.capture();
        } else if (event instanceof ChallengeSignedEvent) {
            return currentValue.sign();
        } else if (event instanceof ChallengeUsedForLoginEvent) {
            return currentValue.invalidate();
        }
        LOGGER.error("an event with a not supported yet type received : {} ", event);
        return null;
    }
}
