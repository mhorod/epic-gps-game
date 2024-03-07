package gps.tracker;

public class OpenStreetMapTranslator {
    private static int base2Power(int exp) {
        return 1 << exp;
    }

    private static double secant(double x) {
        return 1 / Math.cos(x);
    }
    private static String generate_url_suffix(double lat, double lon, int zoom) {
        // Using formulas per https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
        int zoom_multiplier = base2Power(zoom);

        double lat_rad = Math.toRadians(lat);

        double x_ratio = (lon + 180) / 360;
        int x_tile = (int) Math.floor(x_ratio * zoom_multiplier);

        double y_trig = Math.tan(lat_rad) + secant(lat_rad);
        double y_ratio = 1 - Math.log(y_trig) / Math.PI;
        y_ratio /= 2;

        int y_tile = (int) Math.floor(y_ratio * zoom_multiplier);

        return zoom + "/" + x_tile + "/" + y_tile + ".png";
    }

    public static String get_tile_url(double lat, double lon, int zoom) {
        return "https://tile.openstreetmap.org/" + generate_url_suffix(lat, lon, zoom);
    }

    private OpenStreetMapTranslator() {
    }
}
