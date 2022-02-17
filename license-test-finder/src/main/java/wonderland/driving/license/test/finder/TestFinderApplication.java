package wonderland.driving.license.test.finder;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class TestFinderApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFinderApplication.class);
    private WebClient messagePublisherClient;
    private WebClient testFinder;
    private WebClient telegramBotClient;

    @Autowired
    @Qualifier("loadBalancedClient")
    WebClient.Builder loadBalancedWebClientBuilder;

    @Autowired
    @Qualifier("notLoadBalancedClient")
    WebClient.Builder notLoadBalancedWebClientBuilder;

    List<String> people = List.of("Mahdi", "mm7amini@gmail.com");

    public static void main(String[] args) {
        SpringApplication.run(TestFinderApplication.class, args);
    }

    public static final String Find_English_Theory_Exams_Request_Body = """
              {
              "bookingSession": {
                "socialSecurityNumber": "199508020000",
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

    public static final String Find_MANUAL_PRACRICAL_Exams_Request_Body = """
                {
                  "bookingSession": {
                    "socialSecurityNumber": "199508020000",
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
                    "locationId": 1000071,
                    "nearbyLocationIds": [],
                    "vehicleTypeId": 2,
                    "tachographTypeId": 1,
                    "occasionChoiceId": 1,
                    "examinationTypeId": 12
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
        people.stream().forEach(this::requestQueueFor);
        testFinder = notLoadBalancedWebClientBuilder
                .baseUrl("https://fp.trafikverket.se")
                .codecs(codec -> codec.defaultCodecs().maxInMemorySize(2024 * 2024))
                .build();

        telegramBotClient = notLoadBalancedWebClientBuilder
                .baseUrl("https://api.telegram.org/bot5291539544:AAHTAjCZaLYZG4Oc3jMr_Ct5xQnKY77W5xE")
                .codecs(codec -> codec.defaultCodecs().maxInMemorySize(2024 * 2024))
                .build();

        notifyIfFoundExamOnFeb16th()
                .map(Occasion::summary)
                .subscribe(System.out::println);


        Flux.interval(Duration.ofMinutes(30))
                .flatMapSequential(ignore -> notifyIfFoundExamOnFeb16th())
                .map(Occasion::summary)
                .subscribe(System.out::println);
    }

    private Flux<Occasion> notifyIfFoundExamOnFeb16th() {
        return loadExams()
                .doOnNext(ignore -> System.out.println("--------------"))
                .filter(AvailableExamsResponse::isOk)
                .map(AvailableExamsResponse::data)
                .flatMapIterable(Data::bundles)
                .flatMapIterable(Bundle::occasions)
                .filter(Occasion::isAroundUppsala)
                .filter(exam -> exam.date().isAfter(LocalDate.now()))
                .filter(exam -> exam.date().isBefore(LocalDate.now().plusMonths(6)))
                .doOnNext(exam -> {
                    if (exam.date().isAfter(LocalDate.parse("2022-03-01"))
                    && exam.date().isBefore(LocalDate.parse("2022-06-30"))){
                        playSound();
                        var message = "new suitable exam found on " + exam.summary();
                        sendMessage("Mahdi", "mm7amini@gmail.com", message);
                    }
                })
                .doOnNext(exam -> notifyUsingTelegramBot(exam.summary()));
    }

    private void notifyUsingTelegramBot(String text) {
        telegramBotClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "chat_id": "72624148",
                          "text": "%s"
                        }
                        """.formatted(text))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> LOGGER.error("error while sending telegram message", e))
                .subscribe();
    }

    void playSound() {
        try {
            File f = new ClassPathResource("granted.mp3").getFile();
            FileInputStream fileInputStream = new FileInputStream(f);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            Player jlPlayer = new Player(bufferedInputStream);
            new Thread(() -> {
                try {
                    jlPlayer.play();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    private Mono<AvailableExamsResponse> loadExams() {
        return testFinder.post()
                .uri("/Boka/occasion-bundles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Find_MANUAL_PRACRICAL_Exams_Request_Body)
                .header("Referer","https://fp.trafikverket.se/Boka/")
                .header("Origin","https://fp.trafikverket.se")
                .header("sec-ch-ua-platform","\"macOS\"")
                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")

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


