package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = MoversDto.Builder.class)
public class MoversDto {

    private final List<MoverDto> movers;

    public MoversDto(List<MoverDto> movers) {
        this.movers = movers;
    }

    private MoversDto(Builder builder) {
        this.movers = builder.movers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<MoverDto> getMovers() {
        return movers;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MoversDto)) {
            return false;
        }
        MoversDto castOther = (MoversDto) other;
        return Objects.equals(movers, castOther.movers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movers);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("vehicles", movers).toString();
    }

    public static final class Builder {

        private List<MoverDto> movers = Collections.emptyList();

        private Builder() {
        }

        public Builder withMovers(List<MoverDto> val) {
            this.movers = val;
            return this;
        }

        public MoversDto build() {
            return new MoversDto(this);
        }
    }
}
