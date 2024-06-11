package soturi.model;

import java.util.List;

public record Polygon(List<Position> points) {
    public Polygon {
        if (points.size() < 3)
            throw new RuntimeException("attempting to construct invalid polygon");
    }

    // https://wktmap.com/
    public String asWKT() {
        StringBuilder sb = new StringBuilder("POLYGON ((");
        for (Position pos : points)
            sb.append(pos.longitude()).append(" ").append(pos.latitude()).append(", ");

        Position first = points.get(0);
        return sb.append(first.longitude()).append(" ").append(first.latitude()).append("))").toString();
    }
    public static String asWKT(List<Polygon> list) {
        StringBuilder sb = new StringBuilder("GEOMETRYCOLLECTION(");
        boolean first = true;
        for (Polygon poly : list) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(poly.asWKT());
        }
        return sb.append(")").toString();
    }
}
