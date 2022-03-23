

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import feign.FeignException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @ExceptionHandler
  public ResponseEntity<ErrorsDto> handleOrderTravelerProfileException(
      OrderTravelerProfileException e) {
    LOGGER.error("error", e);
    return ResponseEntity.status(e.getStatus())
        .body(ErrorsDto.builder().withErrors(e.getErrors()).build());
  }

  @ExceptionHandler(FeignException.class)
  public void handleFeignException(HttpServletResponse response, FeignException e)
      throws IOException {
    writeError(response, e.status(), ErrorsDto.SERVICE_FAILED, e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public void handleHttpMessageNotReadableException(
      HttpServletResponse response, HttpMessageNotReadableException e) throws IOException {
    writeError(response, 400, ErrorsDto.MESSAGE_NOT_READABLE, e.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public void handleNotFoundException(HttpServletResponse response, NotFoundException e)
      throws IOException {
    LOGGER.error("error", e);
    writeErrors(response, 404, e.getErrors());
  }

  @ExceptionHandler(ConflictException.class)
  public void handleConflictException(HttpServletResponse response, ConflictException e)
      throws IOException {
    writeErrors(response, 409, e.getErrors());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public void handleMethodArgumentNotValidException(
      HttpServletResponse response, MethodArgumentNotValidException e) throws IOException {
    ErrorsDto.Builder errors = ErrorsDto.builder();

    for (ObjectError objectError : e.getBindingResult().getAllErrors()) {
      if (objectError instanceof FieldError) {
        errors.withError(
            ErrorDto.builder()
                .withCode(
                    toUpperCaseUnderscore(((FieldError) objectError).getField())
                        + "_"
                        + ErrorsDto.VALIDATION_ERROR)
                .withMessage(objectError.toString())
                .build());
      } else {
        errors.withError(
            ErrorDto.builder()
                .withCode(ErrorsDto.VALIDATION_ERROR)
                .withMessage(objectError.toString())
                .build());
      }
    }

    writeErrors(response, 400, errors.build());
  }

  @ExceptionHandler(RuntimeException.class)
  public void handleRuntimeException(HttpServletResponse response, RuntimeException e)
      throws IOException {
    if (e.getCause() != null && e.getCause() instanceof ConflictException) {
      handleConflictException(response, (ConflictException) e.getCause());
    } else if (e.getCause() != null && e.getCause() instanceof NotFoundException) {
      handleNotFoundException(response, (NotFoundException) e.getCause());
    } else {
      writeError(
          response, 500, ErrorsDto.SERVICE_FAILED, e.getClass().getName() + ": " + e.getMessage());
    }
  }

  @ExceptionHandler(Exception.class)
  public void handleException(HttpServletResponse response, Exception e) throws IOException {
    writeError(response, 500, ErrorsDto.SERVICE_FAILED, e.getMessage());
  }

  private static void writeError(
      HttpServletResponse response, int status, String code, String message) throws IOException {
    ErrorsDto errors =
        ErrorsDto.builder()
            .withError(ErrorDto.builder().withCode(code).withMessage(message).build())
            .build();

    writeErrors(response, status, errors);
  }

  private static void writeErrors(HttpServletResponse response, int status, ErrorsDto errors)
      throws IOException {
    response.setHeader("Content-Type", "application/json");
    response.setStatus(status);
    objectMapper.writeValue(response.getOutputStream(), errors);
  }

  private static String toUpperCaseUnderscore(String input) {
    if (input == null) {
      return null;
    }
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, input);
  }
}
