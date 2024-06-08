package gps.tracker;

import android.content.Context;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Haha
public class FastClusterer extends RadiusMarkerClusterer {
    public FastClusterer(Context ctx) {
        super(ctx);
    }

    @Override
    public ArrayList<StaticCluster> clusterer(MapView mapView) {

        if (mItems.isEmpty()) {
            return new ArrayList<>();
        }

        if (mapView.getZoomLevel() > mMaxClusteringZoomLevel) {
            return mItems.stream()
                    .map((marker -> {
                        StaticCluster cl = new StaticCluster(marker.getPosition());
                        cl.add(marker);
                        return cl;
                    }))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        Marker firstMarker = mItems.get(0);
        StaticCluster onlyCluster = new StaticCluster(firstMarker.getPosition());

        for (Marker m : mItems) {
            onlyCluster.add(m);
        }

        return new ArrayList<>(List.of(onlyCluster));
    }
}
