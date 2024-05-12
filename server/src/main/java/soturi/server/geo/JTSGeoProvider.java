package soturi.server.geo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.springframework.stereotype.Component;
import soturi.common.GeoProvider;
import soturi.model.Position;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class JTSGeoProvider implements GeoProvider {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private Polygon convertToJTS(soturi.model.Polygon p) {
        Stream<Coordinate> coords = p.points().stream().map(this::convertToJTS);
        Stream<Coordinate> closed = Stream.concat(coords, Stream.of(convertToJTS(p.points().get(0))));
        Coordinate[] coordsArray = closed.toArray(Coordinate[]::new);
        return geometryFactory.createPolygon(coordsArray);
    }
    private Coordinate convertToJTS(Position p) {
        return new Coordinate(p.longitude(), p.latitude());
    }

    private List<soturi.model.Polygon> convertFromJTS(Geometry g) {
        if (g.getDimension() < 2)
            return List.of();
        if (g instanceof Polygon p)
            return List.of(convertFromJTS(p));
        if (g instanceof GeometryCollection gc)
            return IntStream.range(0, gc.getNumGeometries()).mapToObj(gc::getGeometryN)
                .flatMap(geo -> convertFromJTS(geo).stream()).toList();

        throw new RuntimeException("this geometry is strange " + g);
    }
    private soturi.model.Polygon convertFromJTS(Polygon p) {
        if (p.getNumInteriorRing() > 0)
            throw new RuntimeException("polygon with holes");
        List<Position> positions = Arrays.stream(p.getExteriorRing().getCoordinates())
            .skip(1)
            .map(this::convertFromJTS).toList();
        return new soturi.model.Polygon(positions);
    }
    private Position convertFromJTS(Coordinate p) {
        return new Position(p.getY(), p.getX());
    }

    @Override
    public List<soturi.model.Polygon> intersect(soturi.model.Polygon a, soturi.model.Polygon b) {
        Polygon ax = convertToJTS(a);
        Polygon bx = convertToJTS(b);
        return convertFromJTS(ax.intersection(bx));
    }

    @Override
    public boolean isEmpty(soturi.model.Polygon poly) {
        return convertToJTS(poly).isEmpty();
    }

    @Override
    public boolean isValid(soturi.model.Polygon poly) {
        return convertToJTS(poly).isValid();
    }

    @Override
    public boolean isInside(soturi.model.Polygon poly, Position position) {
        return convertToJTS(poly).contains(geometryFactory.createPoint(convertToJTS(position)));
    }

    @Override
    public Position randomPoint(soturi.model.Polygon poly) {
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(geometryFactory);
        randomPointsBuilder.setExtent(convertToJTS(poly));
        randomPointsBuilder.setNumPoints(1);
        MultiPoint multiPoint = (MultiPoint) randomPointsBuilder.getGeometry();
        return convertFromJTS(multiPoint.getCoordinates()[0]);
    }

    @Override
    public double calculateArea(soturi.model.Polygon poly) { // TODO add tests
        Position center = soturi.model.Rectangle.envelopeOf(poly).getCenter();
        double centerLat = center.latitude();
        double centerLon = center.longitude();

        List<Position> positions = poly.points();
        Coordinate[] coords = new Coordinate[positions.size() + 1];

        for (int i = 0; i < positions.size(); ++i) {
            double thisLat = positions.get(i).latitude();
            double thisLon = positions.get(i).longitude();

            double dLat = center.distance(new Position(thisLat, centerLon));
            double dLon = center.distance(new Position(centerLat, thisLon));

            coords[i] = new Coordinate(
                thisLat < centerLat ? -dLat : dLat,
                thisLon < centerLon ? -dLon : dLon
            );
        }

        coords[positions.size()] = coords[0];
        return geometryFactory.createPolygon(coords).getArea();
    }
}
