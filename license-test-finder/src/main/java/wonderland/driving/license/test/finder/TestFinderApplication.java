package wonderland.driving.license.test.finder;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

@SpringBootApplication
public class TestFinderApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFinderApplication.class);
    private WebClient messagePublisherClient;
    private WebClient testFinder;

    @Autowired
    @Qualifier("loadBalancedClient")
    WebClient.Builder loadBalancedWebClientBuilder;

    @Autowired
    @Qualifier("notLoadBalancedClient")
    WebClient.Builder notLoadBalancedWebClientBuilder;

    List<String> people = List.of("Mahdi", "Mahroo");

    public static void main(String[] args) {
        SpringApplication.run(TestFinderApplication.class, args);
    }

    String testFinderRequestBody = """
              {
              "bookingSession": {
                "socialSecurityNumber": "199508027555",
                "licenceId": 5,
                "bookingModeId": 0,
                "ignoreDebt": false,
                "ignoreBookingHindrance": false,
                "examinationTypeId": 0,
                "excludeExaminationCategories": [],
                "rescheduleTypeId": 0,
                "paymentIsActive": false,
                "paymentReference": null,
                "paymentUrl": null,
                "searchedMonths": 0
              },
              "occasionBundleQuery": {
                "startDate": "1970-01-01T00:00:00.000Z",
                "searchedMonths": 0,
                "locationId": 1000140,
                "nearbyLocationIds": [1000071],
                "languageId": 4,
                "tachographTypeId": 1,
                "occasionChoiceId": 1,
                "examinationTypeId": 3
              }
            }
            """;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        LOGGER.info("IP Address:- " + inetAddress.getHostAddress());
        LOGGER.info("Host Name:- " + inetAddress.getHostName());

        messagePublisherClient = loadBalancedWebClientBuilder
                .baseUrl("http://message-publisher")
                .build();
//        people.stream().forEach(this::requestQueueFor);
        testFinder = notLoadBalancedWebClientBuilder
                .baseUrl("https://fp.trafikverket.se")
                .codecs(codec -> codec.defaultCodecs().maxInMemorySize(2024 * 2024))
                .build();

        findATest()
                .filter(AvailableExamsResponse::isOk)
                .map(AvailableExamsResponse::data)
                .flatMapIterable(Data::bundles)
                .flatMapIterable(Bundle::occasions)
                .log()
                .doOnNext(exam -> System.out.println(exam.isInStockholmCity()))
                .subscribe();
    }

    private Mono<AvailableExamsResponse> findATest() {
        return testFinder.post()
                .uri("/Boka/occasion-bundles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testFinderRequestBody)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AvailableExamsResponse.class);
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


