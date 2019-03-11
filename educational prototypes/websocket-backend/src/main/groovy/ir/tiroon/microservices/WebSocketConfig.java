package ir.tiroon.microservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class WebSocketConfig {

    @Bean
    HandlerAdapter wsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    Map<String, MessageHandler> connections = new ConcurrentHashMap<>();

    @Autowired
    PublishSubscribeChannel myChannel;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/echo",
                session ->
                        session.send(Mono.just("Hello").map(session::textMessage)).log()
                                .and(
                                        session.receive()
                                                .map(WebSocketMessage::getPayloadAsText)
                                                .map(session::textMessage)
                                                .map(Mono::just)
                                                .map(session::send)
                                                .log()
                                )
        );

        //Its possible to create the flux separately and share it between all users
        map.put("/simple",
                session -> session.send(
                        Flux.interval(Duration.ofSeconds(1))
                                .map(l -> String.format("{ \"value\": %d }", l))
                                .map(session::textMessage)));

        map.put("/dont-know", session -> {
            var publisher = Flux.<WebSocketMessage>create(fluxSink -> {
                connections.put(session.getId(), new ForwardingMessageHandler(session, fluxSink));
                myChannel.subscribe(connections.get(session.getId()));

                //or simply (but then can not release resources in doFinally
//                myChannel.subscribe(message -> fluxSink.next(session.textMessage((String) message.getPayload()+"::"+session.getId())));

            })
            .doFinally(signalType -> {
                myChannel.unsubscribe(connections.get(session.getId()));
                connections.remove(session.getId());
            });
            return session.send(publisher);
        });

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);

        return handlerMapping;
    }
}

class ForwardingMessageHandler implements MessageHandler {

    WebSocketSession session;
    FluxSink<WebSocketMessage> sink;

    public ForwardingMessageHandler(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        this.session = session;
        this.sink = sink;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String payload = message.getPayload() + "::" + session.getId();
        WebSocketMessage webSocketMessage = session.textMessage(payload);
        sink.next(webSocketMessage);
    }
}
