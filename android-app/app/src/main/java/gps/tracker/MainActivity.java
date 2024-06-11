package gps.tracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import gps.tracker.custom_overlays.EnemyOverlay;
import gps.tracker.databinding.ActivityMainBinding;
import gps.tracker.simple_listeners.Notifier;
import gps.tracker.slow_clusterer.Cluster;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import soturi.common.Registry;
import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.FightRecord;
import soturi.model.FightResult;
import soturi.model.ItemId;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.QuestStatus;
import soturi.model.Result;
import soturi.model.Reward;
import soturi.model.messages_to_client.MessageToClientHandler;

public class MainActivity extends AppCompatActivity {
    public final Notifier locationChangeRequestNotifier = new Notifier();
    private final List<LocationListener> sublisteners = new ArrayList<>();
    @Getter
    private final ItemManager itemManager = new ItemManager(this);
    @Getter
    private final EnemyList enemyList = new EnemyList();
    @Getter
    private Cluster topCluster = Cluster.of();
    @Getter
    public Registry gameRegistry;
    public Reward currentReward; // FIXME: Yeah, I love global variables -- it is used in logic for Quests to properly render the loot
    @Getter
    private volatile CurrentQuests currentQuests = null;
    @Getter
    private volatile Player lastMeUpdate = null;
    @Getter
    private WebSocketClient webSocketClient;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    @Getter
    private Location lastLocation;
    @Setter
    private Consumer<List<Enemy>> enemyAppearsConsumer = null;
    @Setter
    private Consumer<List<EnemyOverlay>> enemyDisappearsConsumer = null;
    private FragmentManager fragmentManager;
    private Runnable onDisconnectRunnable = null;
    private Consumer<Player> playerConsumer = null;
    private Runnable onLoggedInRunnable;
    private Timer locationGuardianTimer;
    private GPSGuardianState gpsGuardianState = new GPSGuardianState();
    @Getter
    @Setter
    private Runnable onErrorRunnable;

    public void saveString(String key, String value) {
        try {
            FileOutputStream stream = openFileOutput(key, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(stream);

            writer.write(value);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(String key) {

        try {
            FileInputStream stream = openFileInput(key);
            Scanner scanner = new Scanner(stream);

            return scanner.nextLine();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(NetworkLogger::reportThrowableFromThread);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        requestPermissions();

        Configuration.getInstance().setUserAgentValue("Soturi/0.1");

        IConfigurationProvider mapConfig = Configuration.getInstance();


        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        File tileCache = new File(basePath, "tile");

        mapConfig.setOsmdroidBasePath(basePath);
        mapConfig.setOsmdroidTileCache(tileCache);
    }

    private void requestPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> postRequestPermissions()
                );
        locationPermissionRequest.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

    }

    private boolean locationCanBeChecked() {
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return gps || network;
    }

    private void enableLocationGuardian() {
        locationGuardianTimer = new Timer();

        TimerTask locationGuardianTask = new TimerTask() {
            @Override
            public void run() {
                if (!locationCanBeChecked() && !gpsGuardianState.alertShown) {
                    gpsGuardianState.alertShown = true;
                    runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Oh no!");
                        builder.setMessage("Please, enable GPS to play the game.");
                        builder.setPositiveButton("I've enabled it", (dialog, id) -> {
                            gpsGuardianState.alertShown = false;
                        });
                        builder.create().show();
                    });
                }
            }
        };

        locationGuardianTimer.schedule(locationGuardianTask, 5000, 1000);
    }

    private boolean permissionEnabled(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void postRequestPermissions() {

        if (!permissionEnabled(android.Manifest.permission.ACCESS_FINE_LOCATION) || !permissionEnabled(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.e("main_activity", "Permissions not granted, exiting.");

            runOnUiThread(
                    () -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Oh no!");
                        builder.setMessage("Please, enable location permissions to play the game.");
                        builder.setPositiveButton("I'll think about it", (dialog, id) -> finish());
                        builder.setCancelable(false);
                        builder.create().show();
                    }
            );

            return;
        }

        Log.e("main_activity", "hello world?");

        locationListener = this::processLocation;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.e("main_activity", "providers: " + locationManager.getAllProviders());

        locationManager.requestLocationUpdates(
                "fused",
                1L,
                0.1f,
                locationListener
        );

        Log.e("main_activity", "hello world!!");

        enableLocationGuardian();
    }

    @SuppressLint("MissingPermission")
    private void processLocation(Location location) {
        System.out.println("PROCESS LOCATION" + location.toString());

        for (LocationListener sublistener : sublisteners) {
            sublistener.onLocationChanged(location);
        }

        Log.e("main_activity", location.toString());
        new Thread(() -> doProcessLocation(location)).start();

        // Haha
        locationManager.removeUpdates(locationListener);

        new Thread(
                () -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    runOnUiThread(() -> {
                                locationManager.requestLocationUpdates(
                                        "fused",
                                        1L,
                                        0.1f,
                                        locationListener);
                            }
                    );
                }
        ).start();

    }

    public void registerLocationListener(LocationListener listener) {
        sublisteners.add(listener);
    }

    public void removeLocationListener(LocationListener listener) {
        sublisteners.remove(listener);
    }

    @SneakyThrows
    private void doProcessLocation(Location location) {
        lastLocation = location;
        Log.e("main_activity", "doProcessLocation()");
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (loggedIn()) {
            webSocketClient.send().updateRealPosition(new Position(lat, lng));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void login(String userName, String userPassword, boolean dev) {
        webSocketClient = new WebSocketClient(new MainActivityHandler(), userName, userPassword, dev);
    }

    public void logout() {
        if (webSocketClient != null) {
            try {
                webSocketClient.send().disconnect();
            } catch (Exception e) {
                // Do nothing, websocket may be already closed
            }
            webSocketClient = null;
        }
    }

    public boolean loggedIn() {
        return webSocketClient != null;
    }

    public void onDisconnect() {
        this.webSocketClient = null;
        enemyList.clear();
        topCluster = Cluster.of();

        if (onDisconnectRunnable != null) {
            onDisconnectRunnable.run();
            onDisconnectRunnable = null;
        }
    }

    public void setOnDisconnect(Runnable runnable) {
        this.onDisconnectRunnable = runnable;
    }

    public void setOnLoggedIn(Runnable runnable) {
        this.onLoggedInRunnable = runnable;
    }

    public void setOnMeUpdate(Consumer<Player> consumer) {
        this.playerConsumer = consumer;
    }

    private static class GPSGuardianState {
        public boolean alertShown = false;
    }

    class MainActivityHandler implements MessageToClientHandler {

        @Override
        public void disconnect() {
            onDisconnect();
        }

        @Override
        public synchronized void enemiesAppear(@NonNull List<Enemy> enemies) {
            if (enemies.size() >= 1000)
                webSocketClient.send().pong();
            for (Enemy enemy : enemies) {
                enemyList.addEnemy(enemy, null);
            }

            if (enemyAppearsConsumer == null) {
                return;
            }

            enemyAppearsConsumer.accept(enemies);
        }

        @Override
        public synchronized void enemiesDisappear(@NonNull List<EnemyId> enemyIds) {
            if (enemyIds.size() >= 1000)
                webSocketClient.send().pong();
            if (enemyAppearsConsumer == null || enemyDisappearsConsumer == null) {
                return;
            }
            List<EnemyOverlay> overlays = enemyIds.stream().map(enemyList::getOverlay).collect(Collectors.toList());
            enemyDisappearsConsumer.accept(overlays);
            enemyIds.forEach(enemyList::removeEnemy);
        }

        @Override
        public void error(String error) {
            if (onErrorRunnable != null) {
                System.out.println("Error: " + error);
                System.out.println("Error runnable is not null");
                onErrorRunnable.run();
            } else {
                System.out.println("Error: " + error);
                System.out.println("Error runnable is null");
            }

            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error");
                builder.setMessage(error);
                builder.setPositiveButton("Fine", (dialog, id) -> {
                });
                builder.create().show();
            });
        }

        @Override
        public void fightDashboardInfo(FightRecord fightRecord) {
            throw new RuntimeException();
        }

        @Override
        public void fightInfo(EnemyId enemyId, FightResult fightResult) {
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                if (fightResult.result() == Result.WON) {
                    String xpString = "XP: " + fightResult.reward().xp();


                    String lootString = fightResult.reward().items().stream()
                            .reduce("",
                                    (acc, item) -> acc + gameRegistry.getItemById(new ItemId(item.id())).name() + "\n",
                                    String::concat);

                    if (lootString.isEmpty()) {
                        lootString = "No loot :(";
                    } else {
                        lootString = "Loot:\n" + lootString;
                    }

                    builder.setMessage("You've won!\n" + xpString + "\n" + lootString);
                    builder.setPositiveButton("Yay!", (dialog, id) -> {
                    });
                } else {
                    builder.setMessage("You've lost!");
                    builder.setPositiveButton("Quite the predicament", (dialog, id) -> {
                    });
                }
                builder.create().show();

            });
        }

        @Override
        public synchronized void meUpdate(Player me) {
            lastMeUpdate = me;

            if (onLoggedInRunnable != null) {
                onLoggedInRunnable.run();
                onLoggedInRunnable = null;
            }

            itemManager.setInventoryItemIDs(me.inventory());
            itemManager.setEquippedItemIDs(me.equipped());

            if (playerConsumer != null) {
                playerConsumer.accept(me);
            }
        }

        @Override
        public synchronized void ping() {
            webSocketClient.send().pong();
        }

        @Override
        public void playerDisappears(String playerName) {

        }

        @Override
        public void playerUpdate(Player player, Position position) {

        }

        @Override
        public void pong() {

        }

        @Override
        public void questUpdate(Instant deadline, List<QuestStatus> quests) {
            currentQuests = new CurrentQuests(deadline, quests);
        }

        @Override
        public void setConfig(Config config) {
            gameRegistry = new Registry(config, null);
        }

    }


}