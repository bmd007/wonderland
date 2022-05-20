package statefull.geofencing.faas.location.aggregate.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class CoordinateDto {

    private final double latitude;
    private final double longitude;

    @JsonCreator
    public CoordinateDto(@JsonProperty("latitude") double latitude, @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CoordinateDto)) {
            return false;
        }
        CoordinateDto castOther = (CoordinateDto) other;
        return Objects.equals(latitude, castOther.latitude) && Objects.equals(longitude, castOther.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("latitude", latitude).add("longitude", longitude).toString();
    }
}
