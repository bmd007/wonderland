package statefull.geofencing.faas.realtime.fencing.dto;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ErrorsDto {

    private List<ErrorDto> errors;

    public ErrorsDto() {
    }

    public static ErrorsDto singleError(String code, String message) {
        return ErrorsDto.builder()
                .withError(ErrorDto.builder()
                        .withCode(code)
                        .withMessage(message)
                        .build())
                .build();
    }

    private ErrorsDto(Builder builder) {
        this.errors = unmodifiableList(builder.errors);
    }

    public List<ErrorDto> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errors", errors)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<ErrorDto> errors = new ArrayList<>();

        public Builder withError(ErrorDto error) {
            errors.add(error);
            return this;
        }

        public Builder withErrors(List<ErrorDto> errors) {
            this.errors = errors;
            return this;
        }

        public Boolean isEmpty() {
            return errors.isEmpty();
        }

        public ErrorsDto build() {
            return new ErrorsDto(this);
        }
    }
}