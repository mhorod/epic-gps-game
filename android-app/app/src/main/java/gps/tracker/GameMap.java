package gps.tracker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.GroundOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.custom_overlays.CustomOverlay;
import gps.tracker.databinding.GameMapFragmentBinding;
import soturi.model.Enemy;

public class GameMap extends Fragment {

    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;
    private Timer enemyUpdater;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = GameMapFragmentBinding.inflate(inflater, container, false);

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext());
        mapView = new MapView(inflater.getContext(), tileProvider, null);

        mainActivity = (MainActivity) getActivity();

        return mapView;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        IMapController controller = mapView.getController();
        controller.setZoom(19.0);

        // We are going to center the map on the user's location once it is available
        // BUT only once, because we want user to be able to actually move the map around
        // At the same time, spawning his view in the middle of the ocean is not a good idea

        timer = new Timer();
        TimerTask updater = new TimerTask() {
            @Override
            public void run() {
                Location location = mainActivity.getLastLocation();

                if (location != null) {
                    GeoPoint geoLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                    mainActivity.runOnUiThread(() -> controller.setCenter(geoLocation));

                    timer.cancel();
                }

            }
        };

        mainActivity.locationChangeRequestNotifier.registerListener(() -> {
                    Location location = mainActivity.getLastLocation();

                    if (location != null) {
                        GeoPoint geoLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mainActivity.runOnUiThread(() -> controller.setCenter(geoLocation));
                    }

                }
        );

        // Proof of concept for updating positions of enemies
        timer.scheduleAtFixedRate(updater, 0, 1000);

        TimerTask updateEnemies = new TimerTask() {
            @Override
            public void run() {
                Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);

                EnemyTracker enemyTracker = mainActivity.getEnemyTracker();
                mapView.getOverlays().clear();

                List<Overlay> overlays = new ArrayList<>();

                for (Enemy enemy : enemyTracker.getEnemies()) {
                    CustomOverlay overlay = new CustomOverlay(mapView, enemy.position(), d);

                    overlay.enableMyLocation();

                    overlays.add(overlay);
                }

                MyLocationNewOverlay myLocation = new MyLocationNewOverlay(new IMyLocationProvider() {
                    @Override
                    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
                        return true;
                    }

                    @Override
                    public void stopLocationProvider() {

                    }

                    @Override
                    public Location getLastKnownLocation() {
                        return mainActivity.getLastLocation();
                    }

                    @Override
                    public void destroy() {

                    }
                }, mapView);
                myLocation.enableMyLocation();

                overlays.add(myLocation);


                mainActivity.runOnUiThread(() -> {
                    mapView.getOverlays().clear();
                    mapView.getOverlays().addAll(overlays);
                    mapView.invalidate();
                });
            }
        };

        enemyUpdater = new Timer();
        enemyUpdater.scheduleAtFixedRate(updateEnemies, 0, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        timer.cancel();
    }

}