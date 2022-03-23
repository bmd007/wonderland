package statefull.geofencing.faas.realtime.fencing.dto;

import com.google.common.base.MoreObjects;

public class ErrorDto {

    private String code;

    private String message;

    public ErrorDto() {
    }

    private ErrorDto(Builder builder) {
        this.code = builder.code;
        this.message = builder.message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("message", message)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String code;
        private String message;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public ErrorDto build() {
            return new ErrorDto(this);
        }
    }
}