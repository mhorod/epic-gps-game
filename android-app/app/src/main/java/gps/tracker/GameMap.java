package gps.tracker;

import android.app.AlertDialog;
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

import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.custom_overlays.EnemyOverlay;
import gps.tracker.databinding.GameMapFragmentBinding;
import soturi.model.Enemy;

public class GameMap extends Fragment {

    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;

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

        overlay.setOnSingleTapConfirmed(() -> {
            mainActivity.runOnUiThread(() -> {
                // New alert dialog
                // https://developer.android.com/guide/topics/ui/dialogs

                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                builder.setMessage("Enemy " + e.enemyId().id() + " appeared!");
                builder.setPositiveButton("OK", (dialog, id) -> {
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        });

        mainActivity.runOnUiThread(() -> {
            mapView.getOverlays().add(overlay);
            mapView.invalidate();
        });
    }

}