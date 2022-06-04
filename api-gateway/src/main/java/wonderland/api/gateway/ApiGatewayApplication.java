package wonderland.api.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        potentialDancePartners.addAll(Set.of("brucee", "camila", "dancer", "jlo", "johnny", "like", "match", "michel", "taylor"));
    }
    private static Map<String, Map<String, LocalDateTime>> likedDancers = new HashMap<>();
    private static Map<String, Map<String, LocalDateTime>> disLikedDancers = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @MessageMapping("names")//todo support time in the searches
    public Flux<String> names(String dancerPartnerSeekerName) {
        var likedDancersByPartnerSeeker = Optional.ofNullable(likedDancers.get(dancerPartnerSeekerName))
                .map(Map::entrySet)
                .orElseGet(() -> Set.of());
        var disLikedDancersByPartnerSeeker = Optional.ofNullable(disLikedDancers.get(dancerPartnerSeekerName))
                .map(Map::entrySet)
                .orElseGet(() -> Set.of());
        log.info("likees {}", likedDancers);
        log.info("desLikees {}", disLikedDancers);
        return Flux.fromIterable(potentialDancePartners)
                .filter(dancerName -> !likedDancersByPartnerSeeker.contains(dancerName))
                .filter(dancerName -> !disLikedDancersByPartnerSeeker.contains(dancerName))
                .log();
    }

    @MessageMapping("addName")
    public Mono<Void> addName(String name) {
        potentialDancePartners.add(name);
        log.info("current dancers,{}",  potentialDancePartners);
        return Mono.empty();
    }

    @MessageMapping("like")
    public Mono<Void> likeADancer(String whoHasLiked, String whomIsLiked) {
        var newLikee = Stream.of(Map.entry(whomIsLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(likedDancers.get(whoHasLiked))
                .orElseGet(() -> Map.of()).entrySet().stream();
        Map<String, LocalDateTime> newLikeesMap = Stream.concat(newLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        likedDancers.put(whoHasLiked, newLikeesMap);
        return Mono.empty();
    }

    @MessageMapping("disLike")
    public Mono<Void> disLikeADancer(String whoHasDisLiked, String whomIsDisLiked) {
        var newDisLikee = Stream.of(Map.entry(whoHasDisLiked, LocalDateTime.now()));
        var alreadyLikeLikeesStream = Optional.ofNullable(disLikedDancers.get(whomIsDisLiked))
                .orElseGet(() -> Map.of()).entrySet().stream();
        var newLikeesMap = Stream.concat(newDisLikee, alreadyLikeLikeesStream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        disLikedDancers.put(whoHasDisLiked, newLikeesMap);
        return Mono.empty();
    }

}
