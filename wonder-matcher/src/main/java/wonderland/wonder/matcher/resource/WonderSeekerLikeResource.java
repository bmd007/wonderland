package wonderland.wonder.matcher.resource;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import wonderland.wonder.matcher.dto.WonderSeekerLikesDto;
import wonderland.wonder.matcher.dto.WonderSeekersLikesDto;
import wonderland.wonder.matcher.exception.NotFoundException;
import wonderland.wonder.matcher.service.ViewService;
import wonderland.wonder.matcher.service.WonderSeekerLikeViewService;

@RestController
@RequestMapping("/api/like")
public class WonderSeekerLikeResource {

    private WonderSeekerLikeViewService wonderSeekerLikeViewService;

    public WonderSeekerLikeResource(WonderSeekerLikeViewService wonderSeekerLikeViewService) {
        this.wonderSeekerLikeViewService = wonderSeekerLikeViewService;
    }


    //isHighLevelQuery query param is related to inter instance communication and it should be true in normal operations or not defined
    @GetMapping
    public Mono<WonderSeekersLikesDto> getCounters(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return wonderSeekerLikeViewService.getAll(isHighLevelQuery);
    }


    @GetMapping("/{wonderSeeker}")
    public Mono<WonderSeekerLikesDto> getCounterByName(@PathVariable("wonderSeeker") String wonderSeeker) {
        return wonderSeekerLikeViewService.getById(wonderSeeker)
                .switchIfEmpty(Mono.error(new NotFoundException(String.format("%s not found (%s doesn't exist).", "Wonder Seeker Likes", wonderSeeker))));
    }
}
