package statefull.geofencing.faas.realtime.fencing.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorDto;
import statefull.geofencing.faas.realtime.fencing.dto.ErrorsDto;

import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

//@Order needed since at least since spring boot 1.2.4. Without it our desired response codes wont be applied
//The lower the value, the higher the priority. The default value has least priority
//-2 as value makes our exception handler to be ?more? effective than spring's one.
@Order(-2)
@Component
public class ExceptionHandler implements WebExceptionHandler {

    private ObjectMapper jsonMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);
    public static final String SERVICE_FAILED = "SERVICE_FAILED";

    public ExceptionHandler(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable e) {
        if (e instanceof ApplicationException){
            return handleApplicationException(exchange, (ApplicationException) e);
        } else if (e instanceof ResponseStatusException) {
            return handleResponseStatusException(exchange, (ResponseStatusException) e);
        } else {
            return handleServerError(exchange, e);
        }
    }

    private Mono<Void> handleApplicationException(ServerWebExchange exchange, ApplicationException e) {
        LOGGER.error("handling", e);
        return writeResponse(exchange, e.getStatus(), toBytes(e.getErrorsDto()));
    }

    private Mono<Void> handleServerError(ServerWebExchange exchange, Throwable e) {
        LOGGER.error("Server error", e);
        var errors = ErrorsDto.builder()
                .withErrors(List.of(ErrorDto.builder()
                        .withCode(SERVICE_FAILED)
                        .withMessage(e.getMessage())
                        .build()))
                .build();
        return writeResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, toBytes(errors));
    }

    private Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException e) {
        LOGGER.debug("Response status exception: {}", e.getMessage());
        var errors = ErrorsDto.builder()
                .withErrors(List.of(ErrorDto.builder()
                        .withCode(e.getStatus().name())
                        .withMessage(e.getMessage())
                        .build()))
                .build();
        return writeResponse(exchange, e.getStatus(), toBytes(errors));
    }

    private byte[] toBytes(ErrorsDto errors) {
        try {
            return jsonMapper.writeValueAsBytes(errors);
        } catch (JsonProcessingException e) {
            return formatDefaultResponse(SERVICE_FAILED, "Cannot serialize error response.");
        }
    }

    private byte[] formatDefaultResponse(String code, String message) {
        return String
                .format("{\"errors\":[{\"code\":\"%s\",\"message\":\"%s\"}]}", code,
                        nullToEmpty(message).replaceAll("\"", "\\\\\""))
                .getBytes();
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, byte[] bytes) {
        var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }
}
