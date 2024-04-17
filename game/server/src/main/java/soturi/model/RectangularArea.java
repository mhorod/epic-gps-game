package soturi.model;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static soturi.model.Position.*;

public record RectangularArea(double lowerLatitude, double upperLatitude, double lowerLongitude, double upperLongitude) {
    public static RectangularArea EARTH = new RectangularArea(minLatitude, maxLatitude, minLongitude, maxLongitude);

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

    private Position proportionalPosition(double pLatitude, double pLongitude) {
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

    private List<RectangularArea> kSplit(int k) { // splits into k**2 areas
        return IntStream
            .range(0, k * k)
            .mapToObj(ij -> new RectangularArea(
                gridPosition(ij / k, ij % k, k),
                gridPosition(ij / k + 1, ij % k + 1, k)
            ))
            .toList();
    }

    public List<RectangularArea> quadSplit() {
        return kSplit(2);
    }
}
