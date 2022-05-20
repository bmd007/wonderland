package wonderland.wonder.matcher.resource;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import statefull.geofencing.faas.common.domain.Mover;
import statefull.geofencing.faas.common.repository.MoverJdbcRepository;
import wonderland.wonder.matcher.config.MetricsFacade;
import wonderland.wonder.matcher.dto.CoordinateDto;
import wonderland.wonder.matcher.dto.MoverDto;
import wonderland.wonder.matcher.dto.MoversDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movers")
public class MoverResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoverResource.class);
    private final MoverJdbcRepository repository;
    private final BiFunction<MoverJdbcRepository, Polygon, List<Mover>> polygonalGeoFencingFunction;
    private final BiFunction<Double, Double, Polygon> wrapLocationByPolygonFunction;
    private final MetricsFacade metricsFacade;

    public MoverResource(MoverJdbcRepository repository,
                         BiFunction<MoverJdbcRepository, Polygon, List<Mover>> polygonalGeoFencingFunction,
                         BiFunction<Double, Double, Polygon> wrapLocationByPolygonFunction, MetricsFacade metricsFacade) {
        this.repository = repository;
        this.polygonalGeoFencingFunction = polygonalGeoFencingFunction;
        this.wrapLocationByPolygonFunction = wrapLocationByPolygonFunction;
        this.metricsFacade = metricsFacade;
    }

    @GetMapping("/{id}")
    public MoverDto get(@PathVariable("id") String id) {
        return map(repository.get(id));
    }

    @GetMapping("/box/by/coordinate")
    public MoversDto queryBox(@RequestParam double latitude,
                              @RequestParam double longitude,
                              @RequestParam(required = false) Long maxAge) {
        var polygon = wrapLocationByPolygonFunction.apply(latitude, longitude);
        try {
            var results =
//                repository.query(polygon)
                    polygonalGeoFencingFunction.apply(repository, polygon)
                            .stream()
                            .map(this::map)
                            .collect(Collectors.toList());
            metricsFacade.incrementQueryByFenceCounter();
            return new MoversDto(results);
        } catch (Exception e) {
            LOGGER.error("error while querying by coordinate", e);
            return MoversDto.builder().withMovers(List.of()).build();
        }
    }

    @PostMapping("/wkt")
    public MoversDto queryPolygon(@RequestBody String wktString, @RequestParam(required = false) Long maxAge) throws ParseException {
        LOGGER.debug("Executing query. MaxAge: {}, Polygon: {}", maxAge, wktString);
        var polygon = (Polygon) repository.getWktReader().read(wktString);
        try {
            var results =
//                repository.query(polygon)
                    polygonalGeoFencingFunction.apply(repository, polygon)
                            .stream()
                            .map(this::map)
                            .collect(Collectors.toList());
            metricsFacade.incrementQueryByFenceCounter();
            return new MoversDto(results);
        } catch (Exception e) {
            LOGGER.error("error while querying by wkt", e);
            return MoversDto.builder().withMovers(List.of()).build();
        }
    }

    private MoverDto map(Mover v) {
        return new MoverDto(v.getId(), new CoordinateDto(v.getLastLocation().getLatitude(), v.getLastLocation().getLongitude()));
    }

    @DeleteMapping
    public void deleteAll() {
        repository.deleteAll();
    }

}
