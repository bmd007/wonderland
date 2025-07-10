package statefull.geofencing.faas.realtime.fencing.dto;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ErrorsDto {

    private List<ErrorDto> errors;

    public static ErrorsDto singleError(String code, String message) {
        return ErrorsDto.builder()
                .withError(ErrorDto.builder()
                        .withCode(code)
                        .withMessage(message)
                        .build())
                .build();
    }
}
