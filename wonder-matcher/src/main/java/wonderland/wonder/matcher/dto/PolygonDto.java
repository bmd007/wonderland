package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = PolygonDto.Builder.class)
public class PolygonDto {

    private final List<CoordinateDto> points;

    private PolygonDto(Builder builder) {
        this.points = builder.points;
    }

    public PolygonDto(List<CoordinateDto> points) {
        this.points = points;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<CoordinateDto> getPoints() {
        return points;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PolygonDto)) {
            return false;
        }
        PolygonDto castOther = (PolygonDto) other;
        return Objects.equals(points, castOther.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("points", points).toString();
    }

    public static final class Builder {

        private final List<CoordinateDto> points = new ArrayList<>();

        private Builder() {
        }

        public Builder addPoint(CoordinateDto point) {
            this.points.add(point);
            return this;
        }

        public Builder withPoints(List<CoordinateDto> points) {
            this.points.clear();
            this.points.addAll(points);
            return this;
        }

        public PolygonDto build() {
            return new PolygonDto(this);
        }
    }
}
