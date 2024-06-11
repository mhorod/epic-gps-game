package gps.tracker.slow_clusterer;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ClusterLeaf extends Cluster {
    private final List<Marker> markers = new ArrayList<>();

    ClusterLeaf(BoundingBox box) {
        super(box);
    }

    @Override
    public void add(Marker m) {
        if (!box.contains(m.getPosition()))
            throw new RuntimeException();
        markers.add(m);
        sumLat += m.getPosition().getLatitude();
        sumLon += m.getPosition().getLongitude();
    }

    @Override
    public void remove(Marker m) {
        if (!markers.remove(m))
            throw new RuntimeException();
        sumLat -= m.getPosition().getLatitude();
        sumLon -= m.getPosition().getLongitude();
    }

    @Override
    public int size() {
        return markers.size();
    }

    @Override
    public Stream<Cluster> getSubClusters() {
        return Stream.of();
    }

    @Override
    public Stream<Marker> getMarkers(BiFunction<GeoPoint, Integer, Marker> markerCreator) {
        return markers.stream();
    }
}
