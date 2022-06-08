package wonderland.wonder.matcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.domain.WonderSeekerMatchHistory;
import wonderland.wonder.matcher.dto.WonderSeekerMatchesDto;
import wonderland.wonder.matcher.dto.WonderSeekersMatchesDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class WonderSeekerMatchViewService extends ViewService<WonderSeekersMatchesDto, WonderSeekerMatchesDto, WonderSeekerMatchHistory> {

    final static Function<WonderSeekersMatchesDto, List<WonderSeekerMatchesDto>> LIST_EXTRACTOR = WonderSeekersMatchesDto::wonderSeekerMatches;
    final static Function<List<WonderSeekerMatchesDto>, WonderSeekersMatchesDto> LIST_WRAPPER = WonderSeekersMatchesDto::new;
    final static BiFunction<String, WonderSeekerMatchHistory, WonderSeekerMatchesDto> DTO_MAPPER =
            (wonderSeekerName, wonderSeekerMatchHistory) ->
                    new WonderSeekerMatchesDto(wonderSeekerMatchHistory.wonderSeekerName(), wonderSeekerMatchHistory.matchHistory());

    public WonderSeekerMatchViewService(StreamsBuilderFactoryBean streams,
                                        @Value("${kafka.streams.server.config.app-ip}") String ip,
                                        @Value("${kafka.streams.server.config.app-port}") int port,
                                        ViewResourcesClient commonClient) {
        super(ip, port, streams, StateStores.WONDER_SEEKER_MATCH_HISTORY_STATE_STORE,
                WonderSeekersMatchesDto.class, WonderSeekerMatchesDto.class,
                DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "/api/match", commonClient);
    }
}
