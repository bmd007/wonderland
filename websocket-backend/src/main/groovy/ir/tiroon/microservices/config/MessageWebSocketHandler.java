package ir.tiroon.microservices.config;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
public class MessageWebSocketHandler implements WebSocketHandler {

    private Flux<Long> messageFlux;

    /**
     * Here we prepare a Flux that will emit a message every second
     */
    @PostConstruct
    private void init() {
        messageFlux = Flux.interval(Duration.ofSeconds(1));
    }

    /**
     * On each new client session, send the message flux to the client.
     * Spring subscribes to the flux and send every new flux event to the WebSocketSession object
     * @param session
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                messageFlux
                        .map(l -> String.format("{ \"value\": %d }", l)) //transform to json
                        .map(session::textMessage)); // map to Spring WebSocketMessage of type text
    }

}