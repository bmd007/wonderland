package ir.tiroon.kafkatoSSE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.reactive.StreamEmitter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@RestController
@SpringBootApplication
@EnableBinding({Source.class})
public class KafkaToSseApplication {


	/*
	* TODO solve scheduler unavailable problem
	* */
	public static void main(String[] args) {
		SpringApplication.run(KafkaToSseApplication.class, args);
	}

	static ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new ParameterNamesModule())
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule());

	String generateRandomEventString(long seed) {
		int x = new Random(seed).nextInt(10);
		int y = new Random(seed).nextInt(10);

		try {
			return objectMapper.writer().forType(Event.class).writeValueAsString(new Event(x, y));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@StreamEmitter
	@Output(Source.OUTPUT)
	public Flux<String> emit() {
		return Flux.interval(Duration.ofSeconds(2))
				.map(this::generateRandomEventString);
	}

	@Bean
	public Flux<ReceiverRecord<String, String>> fluxReceiver() {
		String bootstrapServers = "localhost:9094";
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "click-consumer");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "clickProcessors-group");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		var receiverOptions = ReceiverOptions.<String, String>create(props)
				.subscription(Collections.singleton("clickEvents"));

		return KafkaReceiver.create(receiverOptions).receive().share();
//                .publishOn(Schedulers.parallel()).share();
	}

	@Autowired
	private Flux<ReceiverRecord<String, String>> fluxReceiver;


	////SO this approach has a problem. The application works
	//as far as there is an ongoing flow of data coming from the source
	//So for example if actual message producer suddenly stop producing messages
	//for 1 minutes, then the client gets time outed
	@GetMapping(name = "/clicks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Event> getSimpleFlux() {
		return fluxReceiver.map(KafkaToSseApplication::deserializeFromString)
				.switchIfEmpty(Flux.error(new NullPointerException()));
	}

	public static Event deserializeFromString(ReceiverRecord<String, String> event) {
		try {
			return objectMapper.reader().forType(Event.class).readValue(event.value());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}