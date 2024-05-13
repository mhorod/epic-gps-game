package soturi.common;

import soturi.model.Polygon;
import soturi.model.Position;

import java.util.List;

public interface GeoProvider {
    List<Polygon> intersect(Polygon a, Polygon b);
    boolean isEmpty(Polygon poly);
    boolean isValid(Polygon poly);
    boolean isInside(Polygon poly, Position position);
    Position randomPoint(Polygon poly);
    double calculateArea(Polygon poly);
}
