package wonderland.api.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@SpringBootApplication
public class ApiGatewayApplication {

    private static Set<String> potentialDancePartners = new HashSet<>();

    static{
        potentialDancePartners.add("brucee");
        potentialDancePartners.add("camila");
        potentialDancePartners.add("jlo");
        potentialDancePartners.add("johnny");
        potentialDancePartners.add("michel");
        potentialDancePartners.add("taylor");
    }
    private static Map<String, Map<String, LocalDateTime>> likedDancers = new HashMap<>();
    private static Map<String, Map<String, LocalDateTime>> disLikedDancers = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @MessageMapping("names")//todo support time in the searches
    public Flux<String> names(String dancerPartnerSeekerName) {
        var likedDancersByPartnerSeeker = Optional.ofNullable(likedDancers.get(dancerPartnerSeekerName))
                .map(Map::keySet)
                .orElseGet(Set::of);
        var disLikedDancersByPartnerSeeker = Optional.ofNullable(disLikedDancers.get(dancerPartnerSeekerName))
                .map(Map::keySet)
                .orElseGet(Set::of);
        log.info("likees {}", likedDancersByPartnerSeeker);
        log.info("disLikees {}", disLikedDancersByPartnerSeeker);
        return Flux.fromIterable(potentialDancePartners)
                .filter(dancerName -> !likedDancersByPartnerSeeker.contains(dancerName))
                .filter(dancerName -> !disLikedDancersByPartnerSeeker.contains(dancerName))
                .filter(dancerName -> !dancerName.equals(dancerPartnerSeekerName))
                .doOnNext(System.out::println);
    }

    @MessageMapping("addName")
    public Mono<Void> addName(String name) {
        potentialDancePartners.add(name);
        log.info("current dancers,{}",  potentialDancePartners);
        return Mono.empty();
    }

    record LikeRequestBody(String whoHasLiked, String whomIsLiked){}

    @MessageMapping("like")
    public Mono<Void> likeADancer(LikeRequestBody requestBody) {
        log.info("{}  liked  {}", requestBody.whoHasLiked, requestBody.whomIsLiked);
        var newLikee = Stream.of(Map.entry(requestBody.whomIsLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(likedDancers.get(requestBody.whoHasLiked))
                .orElseGet(Map::of).entrySet().stream();
        Map<String, LocalDateTime> newLikeesMap = Stream.concat(newLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        likedDancers.put(requestBody.whoHasLiked, newLikeesMap);
        log.info("likedDancers {}", likedDancers);
        return Mono.empty();
    }

    record DisLikeRequestBody(String whoHasDisLiked, String whomIsDisLiked){}

    @MessageMapping("disLike")
    public Mono<Void> disLikeADancer(DisLikeRequestBody requestBody) {
        log.info("{} dis liked  {}", requestBody.whoHasDisLiked, requestBody.whomIsDisLiked);
        var newDisLikee = Stream.of(Map.entry(requestBody.whomIsDisLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(disLikedDancers.get(requestBody.whoHasDisLiked))
                .orElseGet(Map::of).entrySet().stream();
        var newLikeesMap = Stream.concat(newDisLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        disLikedDancers.put(requestBody.whoHasDisLiked, newLikeesMap);
        log.info("disLikedDancers {}", disLikedDancers);
        return Mono.empty();
    }

    @MessageMapping("matches")
    public Flux<String> matchStream(){
        return Flux.just("dance", "match").delaySubscription(Duration.ofSeconds(2L));
    }
}
