package gps.tracker.slow_clusterer;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.overlay.Marker;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class Cluster {
    protected final BoundingBox box;
    protected double sumLat = 0;
    protected double sumLon = 0;

    Cluster(BoundingBox box) {
        this.box = box;
    }

    public static Cluster of(BoundingBox box, int lvl) {
        return lvl > 0 ? new ClusterNode(box, lvl) : new ClusterLeaf(box);
    }
    public static Cluster of() {
        TileSystem tileSystem = org.osmdroid.views.MapView.getTileSystem();
        BoundingBox box = new BoundingBox(
                tileSystem.getMaxLatitude(),
                tileSystem.getMaxLongitude(),
                tileSystem.getMinLatitude(),
                tileSystem.getMinLongitude()
        );
        return of(box, 20);
    }

    public abstract void add(Marker m);
    public abstract void remove(Marker m);
    public abstract int size();
    public abstract Stream<Cluster> getSubClusters();
    public abstract Stream<Marker> getMarkers(BiFunction<GeoPoint, Integer, Marker> markerCreator);

    public final BoundingBox getBounds() {
        return box;
    }

    public final GeoPoint getMassCenter() {
        return new GeoPoint(sumLat / size(), sumLon / size());
    }
}
