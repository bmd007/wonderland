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

import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

//todo expose RSocket as well
@RestController
@RequestMapping("/api/wonder")
public class WonderSeekerSeekResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(WonderSeekerSeekResources.class);
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue), 4326);
    private final WonderSeekerJdbcRepository repository;

    public WonderSeekerSeekResources(WonderSeekerJdbcRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Set<WonderSeekerDto> getAll() {
        return repository.getAll()
                .stream()
                .map(this::map)
                .collect(Collectors.toSet());
    }

    @GetMapping("/{id}")
    public WonderSeekerDto get(@PathVariable("id") String id) {
        return map(repository.get(id));
    }

    @GetMapping("/box/by/coordinate")//todo support instant max age, in hours
    public WonderSeekersDto queryBox(@RequestParam double latitude,
                                     @RequestParam double longitude,
                                     @RequestParam double radius,
                                     @RequestParam(required = false) Long maxAge) {
        var point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        /*
        * (distance / 180) * PI * 6371 = radius
        * distance = (radius*180)/(PI*6371)
        * */
        var polygon = (Polygon) point.buffer((radius*180)/(PI*6371));
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
