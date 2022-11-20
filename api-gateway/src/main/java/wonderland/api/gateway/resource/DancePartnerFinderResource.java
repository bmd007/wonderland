package wonderland.api.gateway.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wonderland.api.gateway.config.Topics;
import wonderland.api.gateway.dto.Location;
import wonderland.api.gateway.dto.WonderSeekerDto;
import wonderland.api.gateway.dto.WonderSeekerLikesDto;
import wonderland.api.gateway.dto.WonderSeekerMatchesDto;
import wonderland.api.gateway.dto.WonderSeekersDto;
import wonderland.api.gateway.event.DancePartnerSeekerHasLikedAnotherDancerEvent;
import wonderland.api.gateway.event.DancePartnerSeekerIsDisLikedEvent;
import wonderland.api.gateway.event.DancerIsLookingForPartnerUpdate;
import wonderland.api.gateway.event.Event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
public class DancePartnerFinderResource {

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private static final Map<String, DancerIsLookingForPartnerUpdate> potentialDancePartners = new HashMap<>();
    private final WebClient wonderMatcherClient;

    static {
        potentialDancePartners.put("bruce", new DancerIsLookingForPartnerUpdate("bruce", new Location(59.854216, 17.643421)));
        potentialDancePartners.put("camila", new DancerIsLookingForPartnerUpdate("camila", new Location(59.854253, 17.638438)));
        potentialDancePartners.put("jlo", new DancerIsLookingForPartnerUpdate("jlo", new Location(59.857912, 17.646473)));
        potentialDancePartners.put("johnny", new DancerIsLookingForPartnerUpdate("johnny", new Location(59.857064, 17.627195)));
        potentialDancePartners.put("michel", new DancerIsLookingForPartnerUpdate("michel", new Location(59.874289, 17.627295)));
        potentialDancePartners.put("taylor", new DancerIsLookingForPartnerUpdate("taylor", new Location(59.834210, 17.614565)));
    }

    private static Map<String, Map<String, LocalDateTime>> likedDancers = new HashMap<>();
    private static Map<String, Map<String, LocalDateTime>> disLikedDancers = new HashMap<>();

    public DancePartnerFinderResource(KafkaTemplate<String, Event> kafkaTemplate,
                                      @Qualifier("loadBalancedClient") WebClient.Builder loadBalancedWebClientBuilder) {
        this.kafkaTemplate = kafkaTemplate;
        this.wonderMatcherClient = loadBalancedWebClientBuilder.baseUrl("http://wonder-matcher").build();

        Flux.fromIterable(potentialDancePartners.values())
                .flatMap(event -> Mono.fromFuture(kafkaTemplate.send(Topics.DANCER_SEEKING_PARTNER_UPDATES, event.key(), event)))
                .doOnError(throwable -> log.error("error while publishing"))
                .log()
                .subscribe();
    }

    record GetOtherDancerPartnerSeekersRequestBody(String dancerPartnerSeekerName, Location location, double radius){}
    @MessageMapping("/api/dance/partner/finder/names")
    //todo support time and search circle radius as input params
    //todo create a circle around seeker using radius and convert to wkt
    //todo use wkt end point on wonder matched
    //todo read likes and dislikes from wonder-matcher and zip/filterOut here
    public Flux<String> getOtherDancerPartnerSeekers(GetOtherDancerPartnerSeekersRequestBody requestBody) {
        var likedDancersByPartnerSeeker = Optional.ofNullable(likedDancers.get(requestBody.dancerPartnerSeekerName))
                .map(Map::keySet)
                .orElseGet(Set::of);
        var disLikedDancersByPartnerSeeker = Optional.ofNullable(disLikedDancers.get(requestBody.dancerPartnerSeekerName))
                .map(Map::keySet)
                .orElseGet(Set::of);
        log.info("GetOtherDancerPartnerSeekersRequestBody {}", requestBody);
        log.info("likees {}", likedDancersByPartnerSeeker);
        log.info("disLikees {}", disLikedDancersByPartnerSeeker);
        return wonderMatcherClient.get()
                .uri(uriBuilder -> uriBuilder.path("api/wonder/box/by/coordinate")
                        .queryParam("latitude", requestBody.location.latitude())
                        .queryParam("longitude", requestBody.location.longitude())
                        .queryParam("radius", requestBody.radius)
                        .build())
                .retrieve()
                .bodyToMono(WonderSeekersDto.class)
                .flatMapIterable(WonderSeekersDto::results)
                .map(WonderSeekerDto::wonderSeekerId)
//                .filter(dancerName -> !likedDancersByPartnerSeeker.contains(dancerName))
//                .filter(dancerName -> !disLikedDancersByPartnerSeeker.contains(dancerName))
                .filter(dancerName -> !dancerName.equals(requestBody.dancerPartnerSeekerName))
                .doOnNext(System.out::println);
    }

    record SeekingPartnerRequestBody(String name, Location location) {
    }

    @MessageMapping("/api/dance/partner/finder/addName")
    public Mono<Void> reportSeekingPartner(SeekingPartnerRequestBody requestBody) {
        log.info("current dancers,{}", potentialDancePartners.keySet());
        var event = new DancerIsLookingForPartnerUpdate(requestBody.name, requestBody.location);
        return Mono.fromFuture(kafkaTemplate.send(Topics.DANCER_SEEKING_PARTNER_UPDATES, event.key(), event))
                .doOnError(throwable -> log.error("error while publishing {}", event))
                .log()
                .then();
    }

    record LikeRequestBody(String whoHasLiked, String whomIsLiked) {
    }

    @MessageMapping("/api/dance/partner/finder/like")
    public Mono<Void> likeADancer(LikeRequestBody requestBody) {
        log.info("{}  liked  {}", requestBody.whoHasLiked, requestBody.whomIsLiked);
        var newLikee = Stream.of(Map.entry(requestBody.whomIsLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(likedDancers.get(requestBody.whoHasLiked))
                .orElseGet(Map::of).entrySet().stream();
        Map<String, LocalDateTime> newLikeesMap = Stream.concat(newLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (localDateTime, localDateTime2) ->  localDateTime2));
        likedDancers.put(requestBody.whoHasLiked, newLikeesMap);
        log.info("likedDancers {}", likedDancers);
        var event = new DancePartnerSeekerHasLikedAnotherDancerEvent(requestBody.whoHasLiked, requestBody.whomIsLiked);
        return Mono.fromFuture(kafkaTemplate.send(Topics.DANCE_PARTNER_EVENTS, event.key(), event))
                .doOnError(throwable -> log.error("error while publishing {}", event))
                .log()
                .then();
    }

    @MessageMapping("/api/dance/partner/finder/like/all")
    public Flux<WonderSeekerLikesDto> dancersLikedBy(String wonderSeekerName) {
        return wonderMatcherClient.get()
                .uri("api/like/%s".formatted(wonderSeekerName))
                .retrieve()
                .bodyToFlux(WonderSeekerLikesDto.class);
    }

    @MessageMapping("/api/dance/partner/finder/matches")
    public Flux<String> dancersMatchedWith(String wonderSeekerName) {
        return wonderMatcherClient.get()
                .uri("api/match/%s".formatted(wonderSeekerName))
                .retrieve()
                .bodyToFlux(WonderSeekerMatchesDto.class)
                .log()
                .flatMapIterable(matchesDto -> matchesDto.matchHistory().keySet());
    }

    record DisLikeRequestBody(String whoHasDisLiked, String whomIsDisLiked) {
    }

    @MessageMapping("/api/dance/partner/finder/disLike")
    public Mono<Void> disLikeADancer(DisLikeRequestBody requestBody) {
        log.info("{} dis liked  {}", requestBody.whoHasDisLiked, requestBody.whomIsDisLiked);
        var newDisLikee = Stream.of(Map.entry(requestBody.whomIsDisLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(disLikedDancers.get(requestBody.whoHasDisLiked))
                .orElseGet(Map::of).entrySet().stream();
        var newLikeesMap = Stream.concat(newDisLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (localDateTime, localDateTime2) -> localDateTime2));
        disLikedDancers.put(requestBody.whoHasDisLiked, newLikeesMap);
        log.info("disLikedDancers {}", disLikedDancers);
        var event = new DancePartnerSeekerIsDisLikedEvent(requestBody.whoHasDisLiked, requestBody.whomIsDisLiked);
        return Mono.empty();
//        return Mono.fromFuture(kafkaTemplate.send(Topics.DANCE_PARTNER_EVENTS, event.key(), event).completable())
//                .doOnError(throwable -> log.error("error while publishing {}", event))
//                .log()
//                .then();//todo use another topic for dislikes
    }
}
