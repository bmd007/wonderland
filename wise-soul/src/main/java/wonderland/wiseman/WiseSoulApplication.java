package wonderland.wiseman;

import com.jayway.jsonpath.JsonPath;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class WiseSoulApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiseSoulApplication.class);
    private WebClient messagePublisherClient;
    private WebClient adviceSlipClient;

    @Autowired
    @Qualifier("loadBalancedClient")
    WebClient.Builder loadBalancedWebClientBuilder;

    @Autowired
    @Qualifier("notLoadBalancedClient")
    WebClient.Builder notLoadBalancedWebClientBuilder;

    List<String> people = List.of("Mahdi", "mm7amini@gmail.com");

    public static void main(String[] args) {
        SpringApplication.run(WiseSoulApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        LOGGER.info("IP Address:- " + inetAddress.getHostAddress());
        LOGGER.info("Host Name:- " + inetAddress.getHostName());

        messagePublisherClient = loadBalancedWebClientBuilder
                .baseUrl("http://message-publisher")
                .build();
        adviceSlipClient = notLoadBalancedWebClientBuilder
                .baseUrl("https://api.adviceslip.com")
                .build();

//        safeSleep(80000);
        people.stream().forEach(this::requestQueueFor);
        for (int i = 0; i < 8 ; i++) {
            safeSleep(200);
            var from = aRandomPerson();
            var to = aRandomPerson();
            advice().cache(Duration.ofSeconds(2)).subscribe(text -> {
                LOGGER.info("Sending \"{}\" from {} to {}", text, from, to);
                sendMessage(from, to, text);
            });
        }
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {

    }

    private void safeSleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String aRandomPerson() {
        int nextIndex = Math.abs(new Random().nextInt()) % people.size();
        return people.get(nextIndex);
    }

    private Mono<String> advice() {
        return adviceSlipClient
                .get()
                .uri("/advice")
                .retrieve()
                .bodyToMono(String.class)
                .map(JsonPath::parse)
                .map(documentContext -> documentContext.read("$.slip.advice"))
                .map(String::valueOf)
                .doOnError(throwable -> LOGGER.error("error from NOT load balanced client", throwable));
    }

    private void requestQueueFor(String email) {
        LOGGER.info("requesting creation of queue with name {}", email);
        messagePublisherClient
                .post()
                .uri("/create/queue/for/" + email)
                .retrieve()
                .bodyToMono(String.class)
                .retry(2L)
                .doOnError(throwable -> LOGGER.error("error from load balanced client", throwable))
                .subscribe(s -> LOGGER.info("Answer got by loadBalancedClient from message-publisher : {}", s));
    }

    private void sendMessage(String from, String to, String message) {
        messagePublisherClient
                .post()
                .uri("/send/message/" + from + "/" + to)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .retry(2L)
                .doOnError(throwable -> LOGGER.error("error from load balanced client", throwable))
                .subscribe(s -> LOGGER.info("Answer got by loadBalancedClient from message-publisher : {}", s));
    }
}
