package soturi.model;

import java.util.Map;
import java.util.Random;

import static java.lang.Math.*;
import static soturi.model.Position.*;

public record RectangularArea(double lowerLatitude, double upperLatitude, double lowerLongitude, double upperLongitude) {
    public RectangularArea {
        if (!(minLatitude <= lowerLatitude && lowerLatitude <= upperLatitude &&
              upperLatitude <= maxLatitude))
            throw new RuntimeException(
                "incorrect latitude range: [%f, %f]".formatted(lowerLatitude, upperLatitude));
        if (!(minLongitude <= lowerLongitude && lowerLongitude <= upperLongitude &&
              upperLongitude <= maxLongitude))
            throw new RuntimeException(
                "incorrect longitude range: [%f, %f]".formatted(lowerLongitude, upperLongitude));
    }

    public RectangularArea(Position p1, Position p2) {
        this(
            min(p1.latitude(), p2.latitude()),
            max(p1.latitude(), p2.latitude()),
            min(p1.longitude(), p2.longitude()),
            max(p1.longitude(), p2.longitude())
        );
    }

    public boolean isInside(Position pos) {
        return
            lowerLatitude <= pos.latitude() && pos.latitude() <= upperLatitude &&
            lowerLongitude <= pos.longitude() && pos.longitude() <= upperLongitude;
    }

    public RectangularArea moveCenter(Position newCenter) {
        Position oldCenter = getCenter();
        double diffLatitude = newCenter.latitude() - oldCenter.latitude();
        double diffLongitude = newCenter.longitude() - oldCenter.longitude();
        return new RectangularArea(
            lowerLatitude + diffLatitude,
            upperLatitude + diffLatitude,
            lowerLongitude + diffLongitude,
            upperLongitude + diffLongitude
        );
    }

    public Position proportionalPosition(double pLatitude, double pLongitude) {
        double lat = lowerLatitude + (upperLatitude - lowerLatitude) * pLatitude;
        double lon = lowerLongitude + (upperLongitude - lowerLongitude) * pLongitude;
        return new Position(lat, lon);
    }

    public Position getCenter() {
        return proportionalPosition(0.5, 0.5);
    }

    public Position randomPosition(Random rnd) {
        return proportionalPosition(rnd.nextDouble(), rnd.nextDouble());
    }

    private Position gridPosition(int i, int j, int gridSize) { // i, j are from [0, gridSize]
        return proportionalPosition(1.0 * i / gridSize, 1.0 * j / gridSize);
    }

    public RectangularArea[][] kSplit(int k) { // splits into k**2 areas
        RectangularArea[][] areas = new RectangularArea[k][k];
        for (int i = 0; i < k; ++i)
            for (int j = 0; j < k; ++j)
                areas[i][j] = new RectangularArea(gridPosition(i, j, k), gridPosition(i + 1, j + 1, k));
        return areas;
    }

    public Position[][] getCorners() {
        Position[][] corners = new Position[2][2];
        for (int i = 0; i < 2; ++i)
            for (int j = 0; j < 2; ++j)
                corners[i][j] = proportionalPosition(i, j);
        return corners;
    }

}
