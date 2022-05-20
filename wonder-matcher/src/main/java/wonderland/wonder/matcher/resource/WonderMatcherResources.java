package wonderland.wonder.matcher.resource;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import wonderland.wonder.matcher.domain.Location;
import wonderland.wonder.matcher.domain.WonderSeeker;
import wonderland.wonder.matcher.dto.WonderSeekerDto;
import wonderland.wonder.matcher.dto.WonderSeekersDto;
import wonderland.wonder.matcher.repository.WonderSeekerJdbcRepository;

import java.util.stream.Collectors;

//todo expose RSocket as well
@RestController
@RequestMapping("/api/wonder")
public class WonderMatcherResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(WonderMatcherResources.class);
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue), 4326);
    private final WonderSeekerJdbcRepository repository;

    public WonderMatcherResources(WonderSeekerJdbcRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    public WonderSeekerDto get(@PathVariable("id") String id) {
        return map(repository.get(id));
    }

    @GetMapping("/box/by/coordinate")
    public WonderSeekersDto queryBox(@RequestParam double latitude,
                                     @RequestParam double longitude,
                                     @RequestParam(required = false) Long maxAge) {
        var point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        var polygon = (Polygon) point.buffer(0.0005);
        try {
            var results = repository.query(polygon)
                    .stream()
                    .map(this::map)
                    .collect(Collectors.toList());
            return new WonderSeekersDto(results);
        } catch (Exception e) {
            LOGGER.error("error while querying by coordinate", e);
            return WonderSeekersDto.empty();
        }
    }

    @PostMapping("/wkt")
    public WonderSeekersDto queryPolygon(@RequestBody String wktString, @RequestParam(required = false) Long maxAge) throws ParseException {
        LOGGER.debug("Executing query. MaxAge: {}, Polygon: {}", maxAge, wktString);
        var polygon = (Polygon) repository.getWktReader().read(wktString);
        try {
            var results = repository.query(polygon)
                    .stream()
                    .map(this::map)
                    .collect(Collectors.toList());
            return new WonderSeekersDto(results);
        } catch (Exception e) {
            LOGGER.error("error while querying by wkt", e);
            return WonderSeekersDto.empty();
        }
    }

    private WonderSeekerDto map(WonderSeeker wonderSeeker) {
        return new WonderSeekerDto(wonderSeeker.id(), new Location(wonderSeeker.lastLocation().latitude(), wonderSeeker.lastLocation().longitude()));
    }

    @DeleteMapping
    public void deleteAll() {
        repository.deleteAll();
    }

}
