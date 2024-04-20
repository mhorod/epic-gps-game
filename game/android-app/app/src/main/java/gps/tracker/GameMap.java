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

import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.databinding.GameMapFragmentBinding;
import model.Enemy;

public class GameMap extends Fragment {

    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;
    private Timer enemyUpdater;

    // From https://stackoverflow.com/questions/50077917/android-graphics-drawable-adaptiveicondrawable-cannot-be-cast-to-android-graphic
    // by Shashank Holla; CC BY-SA 4.0
    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

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
                Bitmap icon = getBitmapFromDrawable(d);

                EnemyTracker enemyTracker = mainActivity.getEnemyTracker();
                mapView.getOverlays().clear();

                for (Enemy enemy : enemyTracker.getEnemies()) {
                    GeoPoint enemyLocation = new GeoPoint(enemy.position().latitude(), enemy.position().longitude());

                    final double delta = 0.00003;

                    GeoPoint loc1 = new GeoPoint(enemyLocation.getLatitude() + delta, enemyLocation.getLongitude() - delta);
                    GeoPoint loc2 = new GeoPoint(enemyLocation.getLatitude() - delta, enemyLocation.getLongitude() + delta);

                    GroundOverlay overlay = new GroundOverlay();
                    overlay.setImage(icon);
                    overlay.setPosition(loc1, loc2);

                    mapView.getOverlays().add(overlay);

                }

                mainActivity.runOnUiThread(() -> mapView.invalidate());
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