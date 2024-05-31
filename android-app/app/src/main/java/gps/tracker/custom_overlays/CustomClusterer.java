package gps.tracker.custom_overlays;

import android.graphics.Canvas;

import org.osmdroid.bonuspack.clustering.MarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class CustomClusterer extends MarkerClusterer {

    @Override
    public ArrayList<StaticCluster> clusterer(MapView mapView) {
        return null;
    }

    @Override
    public Marker buildClusterMarker(StaticCluster cluster, MapView mapView) {
        return null;
    }

    @Override
    public void renderer(ArrayList<StaticCluster> clusters, Canvas canvas, MapView mapView) {

    }
}