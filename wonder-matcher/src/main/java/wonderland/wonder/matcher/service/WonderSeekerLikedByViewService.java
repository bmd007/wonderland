package wonderland.wonder.matcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import wonderland.wonder.matcher.config.StateStores;
import wonderland.wonder.matcher.domain.WonderSeekerLikedByHistory;
import wonderland.wonder.matcher.dto.WonderSeekerLikedBysDto;
import wonderland.wonder.matcher.dto.WonderSeekersLikedBysDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class WonderSeekerLikedByViewService extends ViewService<WonderSeekersLikedBysDto, WonderSeekerLikedBysDto, WonderSeekerLikedByHistory> {

    final static Function<WonderSeekersLikedBysDto, List<WonderSeekerLikedBysDto>> LIST_EXTRACTOR = WonderSeekersLikedBysDto::likedBys;
    final static Function<List<WonderSeekerLikedBysDto>, WonderSeekersLikedBysDto> LIST_WRAPPER = WonderSeekersLikedBysDto::new;
    final static BiFunction<String, WonderSeekerLikedByHistory, WonderSeekerLikedBysDto> DTO_MAPPER =
            (wonderSeekerName, wonderSeekerMatchHistory) ->
                    new WonderSeekerLikedBysDto(wonderSeekerMatchHistory.wonderSeekerName(), wonderSeekerMatchHistory.likedByHistory());

    public WonderSeekerLikedByViewService(StreamsBuilderFactoryBean streams,
                                          @Value("${kafka.streams.server.config.app-ip}") String ip,
                                          @Value("${kafka.streams.server.config.app-port}") int port,
                                          ViewResourcesClient commonClient) {
        super(ip, port, streams, StateStores.WONDER_SEEKER_LIKED_BY_HISTORY_STATE_STORE,
                WonderSeekersLikedBysDto.class, WonderSeekerLikedBysDto.class,
                DTO_MAPPER, LIST_EXTRACTOR, LIST_WRAPPER, "/api/like", commonClient);
    }
}
