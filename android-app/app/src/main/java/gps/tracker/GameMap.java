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
import androidx.navigation.fragment.NavHostFragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.custom_overlays.EnemyOverlay;
import gps.tracker.databinding.GameMapFragmentBinding;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyType;
import soturi.model.Player;
import soturi.model.Position;

public class GameMap extends Fragment {

    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;
    private MapEventsReceiver mapEventsReceiver;
    private Timer refreshLocationTimer;
    private MyLocationNewOverlay myLocationOverlay;
    private Timer areWeLoggedInTimer;
    private EnemyList enemyList;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = GameMapFragmentBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) getActivity();
        enemyList = mainActivity.getEnemyList();

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext());
        mapView = new MapView(inflater.getContext(), tileProvider, null);


        binding.mapLayout.addView(mapView);

        changeStatsVisibility(View.GONE);

        return binding.getRoot();

    }

    private void changeStatsVisibility(int visibility) {
        binding.atkIcon.setVisibility(visibility);
        binding.hpIcon.setVisibility(visibility);
        binding.defIcon.setVisibility(visibility);
        binding.hpLevel.setVisibility(visibility);
        binding.atkLevel.setVisibility(visibility);
        binding.defLevel.setVisibility(visibility);
        binding.inventoryButton.setVisibility(visibility);
        binding.levelImageView.setVisibility(visibility);
        binding.levelLevel.setVisibility(visibility);
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

    private void onDisconnect() {
        mainActivity.runOnUiThread(() -> {
            System.out.println("Whoopsie! Connection lost! Fall back to the login screen!");

            mainActivity.runOnUiThread(() -> {
                try {
                    NavHostFragment.findNavController(GameMap.this).navigate(R.id.action_gameMap_to_loginFragment);
                } catch (Exception e) {
                    // It happens
                }
            });
        });
    }

    private void startRefreshingLocation() {
        refreshLocationTimer = new Timer();
        TimerTask updater = new TimerTask() {
            @Override
            public void run() {
                Location location = mainActivity.getLastLocation();

                if (location == null) {
                    return;
                }

                mainActivity.runOnUiThread(() -> {
                    if (myLocationOverlay != null) {
                        mapView.getOverlays().remove(myLocationOverlay);
                    }
                    myLocationOverlay = new MyLocationNewOverlay(new MyLocationProvider(), mapView);
                    myLocationOverlay.enableMyLocation();
                    myLocationOverlay.setDrawAccuracyEnabled(false);

                    mapView.getOverlays().add(myLocationOverlay);
                    mapView.invalidate();
                });


            }
        };

        refreshLocationTimer.schedule(updater, 0, 1000);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        System.out.println("GameMap onViewCreated");

        // We don't allow for any funny business when it comes to the map
        List<Enemy> enemies = mainActivity.getEnemyList().getAllEnemies();
        mainActivity.getEnemyList().clear();

        for (Enemy e : enemies) {
            enemyAppearsConsumer(e);
        }

        IMapController controller = mapView.getController();
        controller.setZoom(19.0);

        mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (mainActivity.getLastLocation() == null) {
                    return false;
                }

                System.out.println("Tapped at " + p);
                Enemy e = enemyList.getClosestEnemy(new Position(p.getLatitude(), p.getLongitude()));

                System.out.println("Closest enemy is " + e);

                if (e == null) {
                    return false;
                }

                if (e.position().distance(new Position(p.getLatitude(), p.getLongitude())) < 10 * Math.pow(2, 20 - mapView.getZoomLevelDouble())) {
                    Location userLocation = mainActivity.getLastLocation();
                    Position userPosition = new Position(userLocation.getLatitude(), userLocation.getLongitude());

                    boolean canAttack = userPosition.distance(e.position()) < 50;

                    mainActivity.runOnUiThread(() -> {
                        // Alert
                        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

                        String name = mainActivity.gameRegistry.getEnemyType(e).name();
                        builder.setTitle("Enemy: " + name + " lvl " + e.lvl());

                        if (canAttack) {
                            builder.setMessage("Proceed with an attack?");
                            builder.setPositiveButton("OK", (dialog, id) -> {
                                System.out.println("Attacking enemy " + e.enemyId());
                                new Thread(() -> attackEnemy(e)).start();
                                dialog.dismiss();
                            });
                            builder.setNegativeButton("Nope", (dialog, id) -> {
                                dialog.dismiss();
                            });
                        } else {
                            builder.setMessage("You are too far away to attack this enemy!");
                            builder.setPositiveButton("Quite the predicament", (dialog, id) -> {
                                dialog.dismiss();
                            });
                        }

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });

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
        mainActivity.setEnemyDisappearsConsumer(this::enemyDisappearsConsumer);

        mainActivity.setOnMeUpdate(
                (Player me) -> {
                    String hpString = me.hp() + "/" + me.statistics().maxHp();
                    String atkString = String.valueOf(me.statistics().attack());
                    String defString = String.valueOf(me.statistics().defense());
                    String levelString = String.valueOf(me.lvl());

                    mainActivity.runOnUiThread(() -> {
                        binding.hpLevel.setText(hpString);
                        binding.atkLevel.setText(atkString);
                        binding.defLevel.setText(defString);
                        binding.levelLevel.setText(levelString);

                        changeStatsVisibility(View.VISIBLE);

                    });
                }
        );


        binding.inventoryButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(GameMap.this).navigate(R.id.action_gameMap_to_inventoryFragment);
        });

        binding.findMeButton.setOnClickListener(v -> {
            centerMapOncePossible();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefreshingLocation();

        mainActivity.setOnDisconnect(this::onDisconnect);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivity.setOnMeUpdate(null);
        mainActivity.setEnemyAppearsConsumer(null);
        mainActivity.setEnemyDisappearsConsumer(null);

        binding = null;
        timer.cancel();
    }

    private synchronized void enemyAppearsConsumer(Enemy e) {
        Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
        try {
            EnemyType type = mainActivity.gameRegistry.getEnemyType(e);
            InputStream stream = getClass().getClassLoader().getResourceAsStream(type.gfxName());
            Drawable draw = Drawable.createFromStream(stream, null);
            if (draw != null)
                d = draw;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        EnemyOverlay overlay = new EnemyOverlay(mapView, new DrawableEnemy(d, e));
        overlay.enableMyLocation();

        enemyList.addEnemy(e, overlay);

        mainActivity.runOnUiThread(() -> {
            mapView.getOverlays().add(0, overlay);
            mapView.invalidate();
        });
    }

    private void enemyDisappearsConsumer(EnemyId e) {
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

    class MyLocationProvider implements IMyLocationProvider {
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
    }

}