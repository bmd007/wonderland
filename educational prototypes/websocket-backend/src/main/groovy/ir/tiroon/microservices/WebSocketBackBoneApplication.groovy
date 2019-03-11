package ir.tiroon.microservices


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.support.GenericMessage
import reactor.core.publisher.Flux

import java.time.Duration

@SpringBootApplication
class WebSocketBackBoneApplication{

	/*
	* ToDo how to combine webScoket with spring security and jwt.
	* In a way that session.getId() for webSocket sessions return the user's username.
	* It is still low level in comparision to the help that is available from brokers when using servlet based web sockets, but still it
	* is possible approach to send messages to specific users.
	*
	* Also bringing spring cloud stream bindings into action is exciting
	* */

	@Bean
	@Primary
	PublishSubscribeChannel myChannel(){
        def channel = new PublishSubscribeChannel()

        Flux.interval(Duration.ofSeconds(2))
        .map{String.valueOf(it)}
        .subscribe{channel.send(new GenericMessage<String>(it))}

        return channel
	}

	static void main(String[] args) {
		SpringApplication.run WebSocketBackBoneApplication, args
	}
}
