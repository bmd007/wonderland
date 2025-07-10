package wonderland.wonder.matcher.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import wonderland.wonder.matcher.dto.WonderSeekerMatchesDto;
import wonderland.wonder.matcher.dto.WonderSeekersMatchesDto;
import wonderland.wonder.matcher.exception.NotFoundException;
import wonderland.wonder.matcher.service.ViewService;
import wonderland.wonder.matcher.service.WonderSeekerMatchViewService;

@RestController
@RequestMapping("/api/match")
public class WonderSeekerMatchResource {

    private final WonderSeekerMatchViewService wonderSeekerMatchViewService;

    public WonderSeekerMatchResource(WonderSeekerMatchViewService wonderSeekerMatchViewService) {
        this.wonderSeekerMatchViewService = wonderSeekerMatchViewService;
    }

    //isHighLevelQuery query param is related to inter instance communication and it should be true in normal operations or not defined
    @GetMapping
    public Mono<WonderSeekersMatchesDto> getCounters(@RequestParam(required = false, value = ViewService.HIGH_LEVEL_QUERY_PARAM_NAME, defaultValue = "true") boolean isHighLevelQuery) {
        return wonderSeekerMatchViewService.getAll(isHighLevelQuery);
    }

    @GetMapping("/{wonderSeeker}")
    public Mono<WonderSeekerMatchesDto> getCounterByName(@PathVariable("wonderSeeker") String wonderSeeker) {
        return wonderSeekerMatchViewService.getById(wonderSeeker)
                .switchIfEmpty(Mono.error(new NotFoundException(String.format("%s not found (%s doesn't exist).", "Wonder Seeker Matches", wonderSeeker))));
    }
}
