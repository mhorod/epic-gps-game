package soturi;

import org.junit.jupiter.api.Test;
import soturi.model.Position;
import soturi.model.Rectangle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

public class GeoTests {
    @Test
    void haversine_test() {
        double krakow_to_warszawa = Position.KRAKOW.distance(Position.WARSZAWA);
        assertThat(krakow_to_warszawa).isCloseTo(252.5 * 1000, withinPercentage(1));
    }
    @Test
    void centered_area_test() {
        Position pos = Position.KRAKOW;
        Rectangle area = pos.centeredArea(100);
        Position[][] corners = area.getCorners();

        assertThat(area.getCenter().distance(pos)).isNotNegative().isLessThan(1);
        assertThat(corners[0][0].distance(corners[1][1])).isCloseTo(100 * Math.sqrt(2), withinPercentage(1));
    }
}
