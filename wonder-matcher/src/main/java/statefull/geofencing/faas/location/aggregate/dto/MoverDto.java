package statefull.geofencing.faas.location.aggregate.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class MoverDto {

    private final String id;
    private final CoordinateDto position;

    @JsonCreator
    public MoverDto(@JsonProperty("id") String id,
                    @JsonProperty("position") CoordinateDto position) {
        this.id = id;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public CoordinateDto getPosition() {
        return position;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MoverDto)) {
            return false;
        }
        MoverDto castOther = (MoverDto) other;
        return Objects.equals(id, castOther.id) && Objects.equals(position, castOther.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("countryCode", id)
                .add("position", position)
                .toString();
    }
}
