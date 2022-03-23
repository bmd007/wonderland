package statefull.geofencing.faas.realtime.fencing.exception;

import org.springframework.http.HttpStatus;
import java.util.List;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorDto;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorsDto;


public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private HttpStatus status;
    private List<ErrorDto> errors;

    public ApplicationException(HttpStatus status, String message) {
        this.status = status;
        this.errors = List.of(ErrorDto.builder().withCode(status.toString()).withMessage(message).build());
    }

    public ApplicationException(HttpStatus status, ErrorDto error) {
        this.status = status;
        this.errors = List.of(error);
    }

    public ApplicationException(HttpStatus status, List<ErrorDto> errors) {
        this.status = status;
        this.errors = List.copyOf(errors);
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", status, errors);
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName()+"{" + "status=" + status + ", errors=" + errors + '}';
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ErrorDto> getErrors() {
        return errors;
    }

    public ErrorsDto getErrorsDto() {
        return ErrorsDto.builder().withErrors(errors).build();
    }
}
