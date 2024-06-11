package gps.tracker.slow_clusterer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import gps.tracker.MainActivity;

public class SlowClusterer extends Overlay {
    private final Context ctx;
    private final RadiusMarkerClusterer radiusMarkerClusterer;

    private final Cluster topCluster;
    private int maxIcons = 100;

    public SlowClusterer(MainActivity ctx) {
        this.ctx = ctx;
        this.radiusMarkerClusterer = new RadiusMarkerClusterer(ctx);
        this.topCluster = ctx.getTopCluster();
    }

    public void setMaxIcons(int maxIcons) {
        this.maxIcons = maxIcons;
    }

    private final Map<Integer, Drawable> iconCache = new HashMap<>();
    private Drawable getIcon(int num, MapView mapView) {
        return iconCache.computeIfAbsent(num, ignored -> radiusMarkerClusterer.buildClusterMarker(
                new StaticCluster(new GeoPoint(0f, 0f)) {
                    @Override
                    public int getSize() {
                        return num;
                    }
                }, mapView).getIcon()
        );
    }

    private Marker buildClusterMarker(GeoPoint markerPosition, int num, MapView mapView) {
        Marker m = new Marker(mapView);
        m.setPosition(markerPosition);
        m.setInfoWindow((MarkerInfoWindow) null);
        m.setAnchor(0.5F, 0.5F);
        m.setIcon(getIcon(num, mapView));
        return m;
    }

    public void add(Marker m) {
        topCluster.add(m);
    }

    public void remove(Marker m) {
        topCluster.remove(m);
    }

    public List<Marker> getVisibleMarkers(MapView mapView) {
        if (topCluster.size() == 0)
            return List.of();

        BiFunction<GeoPoint, Integer, Marker> markerCreator = (g, s) -> buildClusterMarker(g, s, mapView);

        List<Cluster> clusters = List.of(topCluster);
        List<Marker> markers = topCluster.getMarkers(markerCreator).collect(Collectors.toList());

        BoundingBox screenBox = mapView.getBoundingBox().increaseByScale(1.125f);
        double pZoom = mapView.getZoomLevelDouble();

        while (true) {
            List<Cluster> nextClusters = clusters
                    .stream()
                    .flatMap(Cluster::getSubClusters)
                    .filter(c -> c.getBounds().overlaps(screenBox, pZoom))
                    .collect(Collectors.toList());

            List<Marker> nextMarkers = nextClusters
                    .stream()
                    .flatMap(c -> c.getMarkers(markerCreator))
                    .filter(m -> screenBox.contains(m.getPosition()))
                    .collect(Collectors.toList());

            if ((nextMarkers.isEmpty() && nextClusters.isEmpty()) || nextMarkers.size() > maxIcons)
                break;
            clusters = nextClusters;
            markers = nextMarkers;
        }

        return markers;
    }

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow)
            return;

        Projection projection = mapView.getProjection();
        for (Marker m : getVisibleMarkers(mapView))
            m.draw(canvas, projection);
    }


    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        for (Marker m : getVisibleMarkers(mapView))
            if (m.onSingleTapConfirmed(event, mapView))
                return true;

        return false;
    }
}
