package soturi.model;

public record Position(double latitude, double longitude) {
    public static double maxLatitude = 90, minLatitude = -maxLatitude, maxLongitude = 180, minLongitude = -maxLongitude;

    public Position {
        if (!(minLatitude <= latitude && latitude <= maxLatitude))
            throw new RuntimeException("incorrect latitude: " + latitude);
        if (!(minLongitude <= longitude && longitude <= maxLongitude))
            throw new RuntimeException("incorrect longitude: " + longitude);
    }

    /** Returns distance between points in meters */
    public double distance(Position other) {
        // https://en.wikipedia.org/wiki/Haversine_formula
        double phi1 = Math.toRadians(latitude), phi2 = Math.toRadians(other.latitude);
        double lambda1 = Math.toRadians(longitude), lambda2 = Math.toRadians(other.longitude);

        double t1 = Math.sin((phi2 - phi1) / 2);
        double t2 = Math.cos(phi1);
        double t3 = Math.cos(phi2);
        double t4 = Math.sin((lambda2 - lambda1) / 2);

        double earthRadius = 6371 * 1000;

        return 2 * earthRadius * Math.asin(Math.sqrt(t1 * t1 + t2 * t3 * t4 * t4));
    }

    /** This should work for relatively small areas */
    public RectangularArea centeredArea(double sideLengthInMeters) {
        double halfSideLengthInMeters = sideLengthInMeters / 2;

        // let's play the epsilon game
        double eps = 1e-2;

        Position aBitHigher = new Position(latitude + eps, longitude);
        double dLatitude = eps * halfSideLengthInMeters / aBitHigher.distance(this);

        Position aBitRight = new Position(latitude, longitude + eps);
        double dLongitude = eps * halfSideLengthInMeters / aBitRight.distance(this);

        return new RectangularArea(
            latitude - dLatitude,
            latitude + dLatitude,
            longitude - dLongitude,
            longitude + dLongitude
        );
    }
}
