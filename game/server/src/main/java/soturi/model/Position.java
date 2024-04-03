package soturi.model;

public record Position(double latitude, double longitude) {
    /** Returns distance between points in meters */
    public double distance(Position other) {
        // https://en.wikipedia.org/wiki/Haversine_formula
        double phi1 = latitude, phi2 = other.latitude;
        double lambda1 = longitude, lambda2 = other.longitude;

        double t1 = Math.sin((phi2 - phi1) / 2);
        double t2 = Math.cos(phi1);
        double t3 = Math.cos(phi2);
        double t4 = Math.sin((lambda2 - lambda1) / 2);

        double earthRadius = 6371 * 1000;

        return 2 * earthRadius * Math.asin(Math.sqrt(t1 * t1 + t2 * t3 * t4 * t4));
    }
}
