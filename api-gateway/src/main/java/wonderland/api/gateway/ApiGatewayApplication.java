package wonderland.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Controller
@SpringBootApplication
public class ApiGatewayApplication {

	private static ArrayList<String> names = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@MessageMapping("names")
	public Flux<String> numbers(){
		return Flux.fromIterable(names);
	}

	@MessageMapping("addName")
	public Mono<Void> addName(String name){
		return Mono.just(name)
				.doOnNext(names::add)
				.log()
				.then();
	}

}
