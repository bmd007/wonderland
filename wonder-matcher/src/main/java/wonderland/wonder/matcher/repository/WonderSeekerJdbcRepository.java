package wonderland.wonder.matcher.repository;


import jakarta.annotation.Nullable;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import wonderland.wonder.matcher.domain.Location;
import wonderland.wonder.matcher.domain.WonderSeeker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


//todo reactify
@Repository
public class WonderSeekerJdbcRepository {

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue), 4326);
    public static final WKTReader wktReader = new WKTReader(GEOMETRY_FACTORY);
    private final JdbcTemplate jdbc;

    public WonderSeekerJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcTemplate getJdbc() {
        return jdbc;
    }

    public WKTReader getWktReader() {
        return wktReader;
    }

    /**
     * Save (insert or update) a {@link WonderSeeker}.
     */
    public void save(WonderSeeker wonderSeeker) {
        jdbc.execute(String.format(
                "merge into wonder_seekers (id, last_location, updated_at)"
                        + " key (id)"
                        + " values ('%s', '%s', '%s');",
                wonderSeeker.id(),
                coordinateToWktPoint(wonderSeeker.lastLocation()),
                wonderSeeker.updatedAt()
        ));
    }

    /**
     * @param key to identify the wonderSeeker.
     * @return a {@link WonderSeeker} or null.
     */
    @Nullable
    public WonderSeeker get(String key) {
        try {
            var sql = String.format("select * from wonder_seekers where id = '%s'", key);
            return jdbc.queryForObject(sql, this::mapRow);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * @return all wonderSeekers.
     */
    public List<WonderSeeker> getAll() {
        return jdbc.query("select * from wonder_seekers;", this::mapRow);
    }

    /**
     * Execute a range query by key (sorted by the string components).
     *
     * @return a list of wonderSeekers where the keys are in range.
     */
    public List<WonderSeeker> getInRange(String from, String to) {
        return jdbc.query("select * from wonder_seekers where (id between ? and ?);", this::mapRow, from, to);
    }

    /**
     * Delete wonderSeeker by key.
     */
    public void delete(String key) {
        jdbc.execute(String.format("delete from wonder_seekers where id = '%s';", key));
    }

    /**
     * Delete all wonderSeekers.
     */
    public void deleteAll() {
        jdbc.execute("delete from wonder_seekers;");
    }

    //todo add support for maxAge

    /**
     * @return how many wonderSeekers have saved.
     */
    public long count() {
        return jdbc.queryForObject("select count(*) from movres;", Long.class);
    }

    /**
     * Query by a polygon.
     *
     * @param polygon a list of at least 3 points.
     * @return a list of wonderSeekers with coordinates inside the polygon.
     */
    //todo add support for maxAge
    public List<WonderSeeker> query(Polygon polygon) {
        var sql = String.format("select * from wonder_seekers where last_location && '%s' ;", polygon.toText());
        return jdbc.query(sql, this::mapRow);
    }

    private WonderSeeker mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WonderSeeker(rs.getString("id"),
                wktPointToCoordinate(rs.getString("last_location")),
                rs.getTimestamp("updated_at").toInstant());
    }

    //todo handle ull fields
    private Location wktPointToCoordinate(String wktPoint) {
        try {
            var point = (Point) wktReader.read(wktPoint);
            return new Location(point.getX(), point.getY());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Cannot parse point string: " + wktPoint);
    }

    //todo handle ull fields
    private String coordinateToWktPoint(Location location) {
        CoordinateXY jtsCoordinate = new CoordinateXY(location.latitude(), location.longitude());
        return GEOMETRY_FACTORY.createPoint(jtsCoordinate).toText();
    }
}
