package gps.tracker;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gps.tracker.custom_overlays.CustomClusterer;
import gps.tracker.custom_overlays.EnemyOverlay;
import gps.tracker.databinding.GameMapFragmentBinding;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyType;
import soturi.model.Player;

public class GameMap extends Fragment {

    private final HashMap<String, Drawable> drawableCache = new HashMap<>();
    private GameMapFragmentBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer timer;
    private MapEventsReceiver mapEventsReceiver;
    private Timer refreshLocationTimer;
    private MyLocationNewOverlay myLocationOverlay;
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

        setStats(getDefaultGraphicalStats());

        return binding.getRoot();

    }

    @Nullable
    private GraphicalStats getDefaultGraphicalStats() {
        String hpString = mainActivity.getString("hp");
        String atkString = mainActivity.getString("atk");
        String defString = mainActivity.getString("def");
        String levelString = mainActivity.getString("level");
        String progressString = mainActivity.getString("progress");

        if (hpString == null || atkString == null || defString == null || levelString == null || progressString == null) {
            return null;
        }

        int progress = (int) (Double.parseDouble(progressString) * 10000);

        return new GraphicalStats(hpString, atkString, defString, levelString, progress);
    }

    private synchronized void setStats(GraphicalStats stats) {
        if (stats == null) {
            return;
        }

        try {

            binding.hpLevel.setText(stats.hp());
            binding.atkLevel.setText(stats.atk());
            binding.defLevel.setText(stats.def());
            binding.levelLevel.setText(stats.level());

            binding.progressBar.setMax(10000);
            binding.progressBar.setProgress(stats.progress());

            changeStatsVisibility(View.VISIBLE);
        } catch (Exception e) {
            // There is a slight chance that due to the multithreaded nature of the app
            // We will attempt a change after destruction of the view here
            // Causing havoc if this error is not caught
            // But if an error is caught, we can just ignore it as the view doesn't exist anymore either way
        }
    }

    private void changeStatsVisibility(int visibility) {
        binding.statsBar.setVisibility(visibility);
        binding.inventoryBar.setVisibility(visibility);
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

    private void onMeUpdate(@NonNull Player me) {
        String hpString = me.hp() + "/" + me.statistics().maxHp();
        String atkString = String.valueOf(me.statistics().attack());
        String defString = String.valueOf(me.statistics().defense());

        long xpInCurrentLevel = me.xp() - mainActivity.gameRegistry.getXpForLvlCumulative(me.lvl());
        long xpForNextLevel = mainActivity.gameRegistry.getXpForNextLvl(me.lvl());

        double progress = me.lvl() == mainActivity.gameRegistry.getMaxLvl() ?
                1.0 : ((double) xpInCurrentLevel) / xpForNextLevel;

        String levelString = me.lvl() + "";

        mainActivity.saveString("hp", hpString);
        mainActivity.saveString("atk", atkString);
        mainActivity.saveString("def", defString);
        mainActivity.saveString("level", levelString);
        mainActivity.saveString("progress", String.valueOf(progress));

        GraphicalStats stats = new GraphicalStats(hpString, atkString, defString, levelString, (int) (progress * 10000));


        mainActivity.runOnUiThread(() -> {
            setStats(stats);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        System.out.println("GameMap onViewCreated");

        // We don't allow for any funny business when it comes to the map
        List<Enemy> enemies = mainActivity.getEnemyList().getAllEnemies();
        mainActivity.getEnemyList().clear();

        new Thread(
                () -> enemyAppearsConsumer(enemies)
        ).start();

        IMapController controller = mapView.getController();
        controller.setZoom(19.0);

        centerMapOncePossible();

        mainActivity.locationChangeRequestNotifier.registerListener(this::centerMapOncePossible);

        // It should always not be null, but just in case
        if (mainActivity.getLastMeUpdate() != null) {
            onMeUpdate(mainActivity.getLastMeUpdate());
        }

        mainActivity.setOnMeUpdate(this::onMeUpdate);


        binding.inventoryButton.setOnClickListener(v -> NavHostFragment.findNavController(GameMap.this).navigate(R.id.action_gameMap_to_inventoryFragment));

        binding.questButton.setOnClickListener(v -> {
            if (mainActivity.getCurrentQuests() == null) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mainActivity);
                builder.setTitle("No quests available");
                mainActivity.runOnUiThread(builder::show);
            } else {
                NavHostFragment.findNavController(GameMap.this).navigate(R.id.action_gameMap_to_questChoice);
            }
        });

        RadiusMarkerClusterer clusterer = new CustomClusterer(mainActivity);
        clusterer.setMaxClusteringZoomLevel(16);
        clusterer.setRadius(1000);

        mainActivity.runOnUiThread(
                () -> mapView.getOverlays().add(clusterer)
        );

        binding.findMeButton.setOnClickListener(v -> centerMapOncePossible());

        mainActivity.setEnemyAppearsConsumer(this::enemyAppearsConsumer);
        mainActivity.setEnemyDisappearsConsumer(this::enemyDisappearsConsumer);


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

    private synchronized Drawable getDrawableResource(String path) {
        return drawableCache.computeIfAbsent(path, ignored -> {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
                return Drawable.createFromStream(stream, null);
            } catch (Exception exc) {
                return ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
            }
        });
    }

    private synchronized void enemyAppearsConsumer(@NonNull List<Enemy> enemies) {
        List<Marker> toAdd = new ArrayList<>();

        for (Enemy e : enemies) {
            EnemyType type = mainActivity.gameRegistry.getEnemyType(e);
            Drawable d = getDrawableResource(type.gfxName());

            EnemyOverlay overlay;

            try {
                overlay = new EnemyOverlay(mapView, new DrawableEnemy(d, e), mainActivity, this::attackEnemy);
            } catch (NullPointerException npe) {
                overlay = null;
            }

            enemyList.addEnemy(e, overlay);
            toAdd.add(overlay);

        }

        mainActivity.runOnUiThread(() -> {
            try {
                RadiusMarkerClusterer clusterer = (RadiusMarkerClusterer) mapView.getOverlays().get(0);

                for (Marker overlay : toAdd) {
                    clusterer.add(overlay);
                }

                clusterer.invalidate();
                mapView.invalidate();
            } catch (Exception e) {
                // I know why, don't worry
            }
        });
    }

    private synchronized void enemyDisappearsConsumer(EnemyId e) {
        EnemyOverlay overlay = enemyList.getOverlay(e);
        RadiusMarkerClusterer clusterer = (RadiusMarkerClusterer) mapView.getOverlays().get(0);

        enemyList.removeEnemy(e);

        mainActivity.runOnUiThread(() -> {
            clusterer.getItems().remove(overlay);
            clusterer.invalidate();
            mapView.invalidate();
        });
    }

    private void attackEnemy(@NonNull Enemy e) {
        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.getWebSocketClient().send().attackEnemy(e.enemyId());
    }

    private record GraphicalStats(String hp, String atk, String def, String level,
                                  int progress) {
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