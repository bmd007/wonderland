package ir.tiroon.microservices

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.ipc.netty.http.client.HttpClientResponse

import java.nio.charset.Charset
import java.util.function.Consumer

@SpringBootApplication
class WebSocketClientApplication implements CommandLineRunner {

    static void main(String[] args) {
        SpringApplication.run(WebSocketClientApplication.class, args)
    }

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")))
                String authHeader = "Basic " + new String(encodedAuth)
                set("Authorization", authHeader)
            }
        }
    }

    @Autowired
    ObjectMapper objectMapper


    @Override
    void run(String... args) {

        def client = new ReactorNettyWebSocketClient()
        client.execute(
                URI.create("ws://localhost:8080/myHandler"),
                { session ->
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.receive().subscribe(new Consumer<WebSocketMessage>() {
                        @Override
                        void accept(WebSocketMessage webSocketMessage) {
                            System.out.println("BMMMD owner:" + webSocketMessage.getPayloadAsText("UTF-8"))
                        }
                    })
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                    session.send(session.textMessage("BMD BMD BMD"))
                })
    }

}