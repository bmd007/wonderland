package wonderland.wonder.matcher.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wonderland.wonder.matcher.dto.WonderSeekerLikedBysDto;
import wonderland.wonder.matcher.dto.WonderSeekersLikedBysDto;
import wonderland.wonder.matcher.exception.NotFoundException;
import wonderland.wonder.matcher.service.ViewService;
import wonderland.wonder.matcher.service.WonderSeekerLikedByViewService;

@RestController
@RequestMapping("/api/like")
public class WonderSeekerLikedByResource {

    private final WonderSeekerLikedByViewService wonderSeekerLikedByViewService;

    public WonderSeekerLikedByResource(WonderSeekerLikedByViewService wonderSeekerLikedByViewService) {
        this.wonderSeekerLikedByViewService = wonderSeekerLikedByViewService;
    }

    //isHighLevelQuery query param is related to inter instance communication, it should be true in normal operations or not defined
    @GetMapping
    public Mono<WonderSeekersLikedBysDto> getLikes(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return wonderSeekerLikedByViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/{wonderSeeker}")
    public Mono<WonderSeekerLikedBysDto> getLikesByWonderSeekerName(@PathVariable("wonderSeeker") String wonderSeeker) {
        return wonderSeekerLikedByViewService.getById(wonderSeeker)
                .switchIfEmpty(Mono.error(new NotFoundException(String.format("%s not found (%s doesn't exist).", "Wonder Seeker Likes", wonderSeeker))));
    }
}
