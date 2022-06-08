package wonderland.wonder.matcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.domain.WonderSeekerLikeHistory;
import wonderland.wonder.matcher.dto.WonderSeekerLikesDto;
import wonderland.wonder.matcher.dto.WonderSeekersLikesDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class WonderSeekerLikeViewService extends ViewService<WonderSeekersLikesDto, WonderSeekerLikesDto, WonderSeekerLikeHistory> {

    final static Function<WonderSeekersLikesDto, List<WonderSeekerLikesDto>> LIST_EXTRACTOR = WonderSeekersLikesDto::likes;
    final static Function<List<WonderSeekerLikesDto>, WonderSeekersLikesDto> LIST_WRAPPER = WonderSeekersLikesDto::new;
    final static BiFunction<String, WonderSeekerLikeHistory, WonderSeekerLikesDto> DTO_MAPPER =
            (wonderSeekerName, wonderSeekerMatchHistory) ->
                    new WonderSeekerLikesDto(wonderSeekerMatchHistory.wonderSeekerName(), wonderSeekerMatchHistory.likeHistory());

    public WonderSeekerLikeViewService(StreamsBuilderFactoryBean streams,
                                       @Value("${kafka.streams.server.config.app-ip}") String ip,
                                       @Value("${kafka.streams.server.config.app-port}") int port,
                                       ViewResourcesClient commonClient) {
        super(ip, port, streams, StateStores.WONDER_SEEKER_LIKE_HISTORY_STATE_STORE,
                WonderSeekersLikesDto.class, WonderSeekerLikesDto.class,
                DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "/api/like", commonClient);
    }
}
