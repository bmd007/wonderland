package wonderland.wonder.matcher.dto;

import wonderland.wonder.matcher.domain.Location;

import java.util.List;

public record PolygonDto(List<Location> points) {
}
