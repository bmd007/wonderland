package wonderland.wonder.matcher.serialization;

import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.dto.WonderSeekerDto;

public class CustomSerdes {
    public static final JsonSerde<WonderSeeker> WONDER_SEEKER_JSON_SERDE = new JsonSerde<>(WonderSeeker.class);
    public static final JsonSerde<WonderSeekerDto> WONDER_SEEKER_DTO_JSON_SERDE = new JsonSerde<>(WonderSeekerDto.class);
}
