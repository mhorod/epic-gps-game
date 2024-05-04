package soturi.server;

import org.junit.jupiter.api.Test;
import soturi.content.GeoRegistry;
import soturi.model.Position;
import soturi.model.RectangularArea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

public class PositionTests {
    @Test
    void haversine_test() {
        double krakow_to_warszawa = GeoRegistry.KRAKOW.distance(GeoRegistry.WARSZAWA);
        assertThat(krakow_to_warszawa).isCloseTo(252.5 * 1000, withinPercentage(5));
    }
    @Test
    void reflection_test() {
        assertThat(GeoRegistry.KRAKOW).isEqualTo(GeoRegistry.KNOWN_POSITIONS.get("KRAKOW"));
        assertThat(GeoRegistry.POLAND).isEqualTo(GeoRegistry.KNOWN_AREAS.get("POLAND"));
    }
    @Test
    void centered_area_test() {
        Position pos = GeoRegistry.KRAKOW;
        RectangularArea area = pos.centeredArea(100);
        Position[][] corners = area.getCorners();

        assertThat(area.getCenter().distance(pos)).isNotNegative().isLessThan(1);
        assertThat(corners[0][0].distance(corners[1][1])).isCloseTo(100 * Math.sqrt(2), withinPercentage(5));
    }
}
