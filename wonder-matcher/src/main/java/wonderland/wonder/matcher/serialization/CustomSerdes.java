package wonderland.wonder.matcher.serialization;

import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.dto.SeekerWonderingUpdateDto;

public class CustomSerdes {
    public static final JsonSerde<WonderSeeker> WONDER_SEEKER_JSON_SERDE = new JsonSerde<>(WonderSeeker.class);
    public static final JsonSerde<SeekerWonderingUpdateDto> WONDER_SEEKER_DTO_JSON_SERDE = new JsonSerde<>(SeekerWonderingUpdateDto.class);
}
