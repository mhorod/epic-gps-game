package soturi.content;

import soturi.model.Position;
import soturi.model.RectangularArea;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeoRegistry {
    private GeoRegistry() {
        throw new RuntimeException();
    }

    public static Position KRAKOW = new Position(50.06143, 19.93658);
    public static Position WARSZAWA = new Position(52.22977, 21.01178);

    public static RectangularArea EARTH = new RectangularArea(-90.0, 90.0, -180.0, 180.0);
    public static RectangularArea POLAND = new RectangularArea(49.15, 55.85, 13.70, 24.30);
    public static RectangularArea KRAKOW_AREA = new RectangularArea(49.97, 50.11, 19.74, 20.09);

    public static RectangularArea WAWEL = new Position(50.05389, 19.93472).centeredArea(120);
    public static RectangularArea BOARS = new Position(50.01190, 19.89906).centeredArea(8);
    public static RectangularArea TCS_UJ = new Position(50.03070, 19.90698).centeredArea(2);

    public static Map<String, Position> KNOWN_POSITIONS;
    public static Map<String, RectangularArea> KNOWN_AREAS;

    static {
        try {
            Map<String, Position> positionMap = new LinkedHashMap<>();
            Map<String, RectangularArea> areaMap = new LinkedHashMap<>();

            for (Field f : GeoRegistry.class.getFields()) {
                if (f.getType() == Position.class)
                    positionMap.put(f.getName(), (Position) f.get(null));
                if (f.getType() == RectangularArea.class)
                    areaMap.put(f.getName(), (RectangularArea) f.get(null));
            }

            KNOWN_POSITIONS = Collections.unmodifiableMap(positionMap);
            KNOWN_AREAS = Collections.unmodifiableMap(areaMap);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
