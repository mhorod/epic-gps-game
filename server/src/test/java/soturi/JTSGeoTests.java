package soturi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import soturi.common.GeoProvider;
import soturi.model.Polygon;
import soturi.model.Position;
import soturi.server.geo.JTSGeoProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(value = {JTSGeoProvider.class})
public class JTSGeoTests {
    @Autowired
    GeoProvider geoProvider;

    @Test
    void intersection_test_1() {
        Polygon a = new Polygon(List.of(
            new Position(0, 1),
            new Position(0, 2),
            new Position(3, 2),
            new Position(3, 1)
        ));
        Polygon b = new Polygon(List.of(
            new Position(1, 0),
            new Position(2, 0),
            new Position(2, 3),
            new Position(1, 3)
        ));
        List<Polygon> intersect = geoProvider.intersect(a, b);
        assertThat(intersect).hasSize(1);
        assertThat(intersect.get(0).points()).hasSize(4);
    }

    @Test
    void intersection_test_2() {
        Polygon a = new Polygon(List.of(
            new Position(0, 0),
            new Position(3, 0),
            new Position(3, 1),
            new Position(0, 1)
        ));
        Polygon b = new Polygon(List.of(
            new Position(0, 0),
            new Position(1, 0),
            new Position(1, 2),
            new Position(2, 2),
            new Position(2, 0),
            new Position(3, 0),
            new Position(3, 3),
            new Position(0, 3)
        ));
        List<Polygon> intersect = geoProvider.intersect(a, b);
        assertThat(intersect).hasSize(2);
        assertThat(intersect.get(0).points()).hasSize(4);
        assertThat(intersect.get(1).points()).hasSize(4);
    }

    @Test
    void intersection_test_3() {
        Polygon a = new Polygon(List.of(
            new Position(0, 0),
            new Position(3, 0),
            new Position(3, 1),
            new Position(0, 1)
        ));
        Polygon b = new Polygon(List.of(
            new Position(0, 0),
            new Position(1, 0),
            new Position(1, 2),
            new Position(1.25, 1),
            new Position(1.5, 2),
            new Position(1.5, 1),
            new Position(1.75, 1),
            new Position(2, 2),
            new Position(2, 0),
            new Position(3, 0),
            new Position(3, 3),
            new Position(0, 3)
        ));
        List<Polygon> intersect = geoProvider.intersect(a, b);
        assertThat(intersect).hasSize(2);
        assertThat(intersect.get(0).points()).hasSize(4);
        assertThat(intersect.get(1).points()).hasSize(4);
    }

    @Test
    void intersection_test_4() {
        Polygon a = new Polygon(List.of(
            new Position(0, 0),
            new Position(1, 0),
            new Position(1, 1),
            new Position(0, 1)
        ));
        Polygon b = new Polygon(List.of(
            new Position(0, 1),
            new Position(1, 1),
            new Position(1, 2),
            new Position(0, 2)
        ));
        List<Polygon> intersect = geoProvider.intersect(a, b);
        assertThat(intersect).hasSize(0);
    }
}
