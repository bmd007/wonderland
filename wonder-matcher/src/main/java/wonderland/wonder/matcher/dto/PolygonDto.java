package wonderland.wonder.matcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PolygonDto(List<CoordinateDto> points){
}
