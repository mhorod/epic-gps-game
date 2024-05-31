package gps.tracker.custom_overlays;

import android.content.Context;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class CustomClusterer extends RadiusMarkerClusterer {

    public CustomClusterer(Context ctx) {
        super(ctx);
    }

    @Override
    public ArrayList<StaticCluster> clusterer(MapView mapView) {
        System.out.println("CustomClusterer.clusterer");
        return super.clusterer(mapView);
    }

}