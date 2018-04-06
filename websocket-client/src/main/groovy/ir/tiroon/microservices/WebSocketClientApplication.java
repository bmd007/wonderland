package ir.tiroon.microservices;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@SpringBootApplication
public class WebSocketClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketClientApplication.class, args);
    }

    @Override
    public void run(String... args) {

        WebSocketClient client = new ReactorNettyWebSocketClient();
        client
          .execute(
            URI.create("ws://localhost:8086/echo"),
            session -> session.send(Mono.just("Salam").map(session::textMessage))
            .thenMany(
                    session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
            )
            .then()
          )
        .subscribe();
    }

}