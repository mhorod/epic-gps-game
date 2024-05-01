package soturi.server;

import org.junit.jupiter.api.Test;
import soturi.model.Position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

public class PositionTests {
    @Test
    void haversineTest() {
        double krakow_to_warszawa = Position.KRAKOW.distance(Position.WARSZAWA);
        assertThat(krakow_to_warszawa).isCloseTo(252.5 * 1000, withinPercentage(5));
    }
}
