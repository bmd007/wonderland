package wonderland.wonder.matcher.serialization;

import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.domain.WonderSeekerLikeHistory;
import wonderland.wonder.matcher.dto.DancePartnerSeekerHasLikedAnotherDancerEvent;
import wonderland.wonder.matcher.dto.DancePartnerSeekerIsLikedByAnotherDancerEvent;
import wonderland.wonder.matcher.dto.DancerIsLookingForPartnerUpdate;
import wonderland.wonder.matcher.dto.SeekerWonderingUpdateDto;

public class CustomSerdes {

    public static final JsonSerde<WonderSeeker> WONDER_SEEKER_JSON_SERDE = new JsonSerde<>(WonderSeeker.class);
    public static final JsonSerde<WonderSeekerLikeHistory> WONDER_SEEKER_LIKE_HISTORY_JSON_SERDE = new JsonSerde<>(WonderSeekerLikeHistory.class);
    public static final JsonSerde<SeekerWonderingUpdateDto> WONDER_SEEKER_DTO_JSON_SERDE = new JsonSerde<>(SeekerWonderingUpdateDto.class);
    public static final JsonSerde<DancerIsLookingForPartnerUpdate> DANCER_SEEKING_PARTNER_JSON_SERDE = new JsonSerde<>(DancerIsLookingForPartnerUpdate.class);
    public static final JsonSerde<DancePartnerSeekerHasLikedAnotherDancerEvent> LIKERS_EVENT_JSON_SERDE = new JsonSerde<>(DancePartnerSeekerHasLikedAnotherDancerEvent.class);
    public static final JsonSerde<DancePartnerSeekerIsLikedByAnotherDancerEvent> LIKEES_EVENT_JSON_SERDE = new JsonSerde<>(DancePartnerSeekerIsLikedByAnotherDancerEvent.class);

}
