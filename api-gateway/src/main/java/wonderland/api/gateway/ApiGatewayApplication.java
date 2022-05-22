package wonderland.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Controller
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@MessageMapping("stream")
	Flux<Long> numbers(){
		return Flux.interval(Duration.ofSeconds(1)).log();
	}

	@MessageMapping("echo")
	Mono<String> echo(String text){
		return Mono.just(text);
	}

}
