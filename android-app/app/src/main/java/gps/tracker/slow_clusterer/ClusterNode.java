package gps.tracker.slow_clusterer;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ClusterNode extends Cluster {
    private final int lvl;
    private int size = 0;
    private Cluster[] subClusters;

    ClusterNode(BoundingBox box, int lvl) {
        super(box);
        this.lvl = lvl;
    }

    private int recursiveDirection(GeoPoint pos) {
        for (int i = 0; i < 4; ++i)
            if (subClusters[i].box.contains(pos))
                return i;
        throw new RuntimeException();
    }

    private void initSubClusters() {
        if (subClusters != null)
            return;
        subClusters = new Cluster[4];
        for (int i = 0; i < 4; ++i) {
            float dx = (i & 1) == 0 ? 0f : 0.5f;
            float dy = (i & 2) == 0 ? 0f : 0.5f;
            GeoPoint p0 = box.getGeoPointOfRelativePositionWithLinearInterpolation(dx, dy);
            GeoPoint p1 = box.getGeoPointOfRelativePositionWithLinearInterpolation(dx + 0.5f, dy + 0.5f);
            BoundingBox subBox = BoundingBox.fromGeoPoints(List.of(p0, p1));
            subClusters[i] = Cluster.of(subBox, lvl - 1);
        }
    }

    private void fixInvariants() {
        size = 0;
        sumLat = 0;
        sumLon = 0;
        for (int i = 0; i < 4; ++i) {
            size += subClusters[i].size();
            sumLat += subClusters[i].sumLat;
            sumLon += subClusters[i].sumLon;
        }
    }

    @Override
    public void add(Marker m) {
        initSubClusters();
        int dir = recursiveDirection(m.getPosition());
        subClusters[dir].add(m);
        fixInvariants();
    }

    @Override
    public void remove(Marker m) {
        int dir = recursiveDirection(m.getPosition());
        subClusters[dir].remove(m);
        fixInvariants();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Stream<Cluster> getSubClusters() {
        if (subClusters == null)
            return Stream.of();
        return Arrays.stream(subClusters).filter(c -> c.size() > 0);
    }

    @Override
    public Stream<Marker> getMarkers(BiFunction<GeoPoint, Integer, Marker> markerCreator) {
        if (size == 0)
            return Stream.of();
        return Stream.of(markerCreator.apply(getMassCenter(), size));
    }
}
