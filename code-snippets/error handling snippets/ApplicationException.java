package statefull.geofencing.faas.realtime.fencing.exception;

import org.springframework.http.HttpStatus;
import java.util.List;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorDto;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorsDto;


public class ApplicationException extends RuntimeException {
    private HttpStatus status;
    private List<ErrorDto> errors;
}
