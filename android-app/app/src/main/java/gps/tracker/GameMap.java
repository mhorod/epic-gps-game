package gps.tracker;

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
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.custom_overlays.EnemyOverlay;
import gps.tracker.databinding.GameMapFragmentBinding;
import soturi.model.Enemy;
import soturi.model.Position;

public class GameMap extends Fragment {

    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;
    private EnemyList enemyList;
    private MapEventsReceiver mapEventsReceiver;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = GameMapFragmentBinding.inflate(inflater, container, false);

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext());
        mapView = new MapView(inflater.getContext(), tileProvider, null);

        mainActivity = (MainActivity) getActivity();

        enemyList = new EnemyList();

        return mapView;

    }

    private void centerMapOncePossible() {
        timer = new Timer();
        TimerTask updater = new TimerTask() {
            @Override
            public void run() {
                Location location = mainActivity.getLastLocation();

                if (location != null) {
                    IMapController controller = mapView.getController();
                    GeoPoint geoLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                    mainActivity.runOnUiThread(() -> controller.setCenter(geoLocation));

                    timer.cancel();
                }

            }
        };

        timer.schedule(updater, 0, 1000);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        IMapController controller = mapView.getController();
        controller.setZoom(19.0);

        mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                System.out.println("Tapped at " + p);
                Enemy e = enemyList.getClosestEnemy(new Position(p.getLatitude(), p.getLongitude()));

                System.out.println("Closest enemy is " + e);

                if (e == null) {
                    return false;
                }

                if (e.position().distance(new Position(p.getLatitude(), p.getLongitude())) < 50000) {
                    System.out.println("Attacking enemy " + e.enemyId());
                    attackEnemy(e);
                    return true;
                }

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        centerMapOncePossible();

        mainActivity.locationChangeRequestNotifier.registerListener(this::centerMapOncePossible);
        mainActivity.setEnemyAppearsConsumer(this::enemyAppearsConsumer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        timer.cancel();
    }

    private void enemyAppearsConsumer(Enemy e) {
        Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
        EnemyOverlay overlay = new EnemyOverlay(mapView, new DrawableEnemy(d, e));
        overlay.enableMyLocation();

        enemyList.addEnemy(e, overlay);

        mainActivity.runOnUiThread(() -> {
            mapView.getOverlays().add(overlay);
            mapView.invalidate();
        });
    }

    private void enemyDisappearsConsumer(Enemy e) {
        EnemyOverlay overlay = enemyList.getOverlay(e);
        enemyList.removeEnemy(e);

        mainActivity.runOnUiThread(() -> {
            mapView.getOverlays().remove(overlay);
            mapView.invalidate();
        });
    }

    private void attackEnemy(Enemy e) {
        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.getWebSocketClient().send().attackEnemy(e.enemyId());
    }

}