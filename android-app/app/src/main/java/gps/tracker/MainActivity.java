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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import gps.tracker.databinding.ActivityMainBinding;
import gps.tracker.simple_listeners.Notifier;
import lombok.SneakyThrows;
import soturi.common.Registry;
import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.FightRecord;
import soturi.model.FightResult;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.MessageToClientHandler;

public class MainActivity extends AppCompatActivity {

    public final Notifier locationChangeRequestNotifier = new Notifier();
    private final List<LocationListener> sublisteners = new ArrayList<>();
    private final ItemManager itemManager = new ItemManager(this);
    private final EnemyList enemyList = new EnemyList();
    public Registry gameRegistry;
    private WebSocketClient webSocketClient;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location lastLocation;
    private Consumer<Enemy> enemyAppearsConsumer = null;
    private Consumer<EnemyId> enemyDisappearsConsumer = null;
    private FragmentManager fragmentManager;
    private Runnable onDisconnectRunnable = null;
    private Consumer<Player> playerConsumer = null;

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

    private void postRequestPermissions() {
        {
            int req = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (req != PackageManager.PERMISSION_GRANTED) {
                Log.e("main_activity", "ups od ACCESS_FINE_LOCATION, status: " + req);
                return;
            }
        }
        {
            int req = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (req != PackageManager.PERMISSION_GRANTED) {
                Log.e("main_activity", "ups od ACCESS_COARSE_LOCATION, status: " + req);
                return;
            }
        }

        Log.e("main_activity", "hello world?");

        locationListener = this::processLocation;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.e("main_activity", "providers: " + locationManager.getAllProviders().toString());

        locationManager.requestLocationUpdates(
                "fused",
                1L,
                0.1f,
                locationListener
        );

        Log.e("main_activity", "hello world!!");
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

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setEnemyAppearsConsumer(Consumer<Enemy> enemyAppearsConsumer) {
        this.enemyAppearsConsumer = enemyAppearsConsumer;
    }

    public void setEnemyDisappearsConsumer(Consumer<EnemyId> enemyDisappearsConsumer) {
        this.enemyDisappearsConsumer = enemyDisappearsConsumer;
    }

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
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

        if (onDisconnectRunnable != null) {
            onDisconnectRunnable.run();
            onDisconnectRunnable = null;
        }
    }

    public void setOnDisconnect(Runnable runnable) {
        this.onDisconnectRunnable = runnable;
    }

    public void setOnMeUpdate(Consumer<Player> consumer) {
        this.playerConsumer = consumer;
    }

    public Registry getGameRegistry() {
        return gameRegistry;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public EnemyList getEnemyList() {
        return enemyList;
    }

    class MainActivityHandler implements MessageToClientHandler {

        private ArrayList<Enemy> enemiesBacklog = new ArrayList<>();

        @Override
        public void disconnect() {
            onDisconnect();
        }

        @Override
        public void enemiesAppear(List<Enemy> enemies) {
            enemies.forEach(this::enemyAppears);
        }

        @Override
        public void enemiesDisappear(List<EnemyId> enemyIds) {
            enemyIds.forEach(this::enemyDisappears);
        }

        private void cleanBacklog(Consumer<Enemy> consumer) {
            for (Enemy enemy : enemiesBacklog) {
                consumer.accept(enemy);
            }
            enemiesBacklog.clear();
        }

        private void enemyAppears(Enemy enemy) {
            if (enemyAppearsConsumer == null) {
                enemiesBacklog.add(enemy);
                return;
            }

            cleanBacklog(enemyAppearsConsumer);
            enemyAppearsConsumer.accept(enemy);

        }

        private void enemyDisappears(EnemyId enemyId) {
            if (enemyAppearsConsumer == null || enemyDisappearsConsumer == null) {
                enemiesBacklog.removeIf(enemy -> enemy.enemyId().equals(enemyId));
                return;
            }
            enemyDisappearsConsumer.accept(enemyId);
        }

        @Override
        public void error(String error) {
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
                    builder.setMessage("You won!");
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
        public void meUpdate(Player me) {
            // Because this event is sent cyclically, we will use it to put enemies from backlog on the map
            if (enemyDisappearsConsumer != null) {
                cleanBacklog(enemyAppearsConsumer);
            }

            itemManager.setInventoryItemIDs(me.inventory());
            itemManager.setEquippedItemIDs(me.equipped());

            if (playerConsumer != null) {
                playerConsumer.accept(me);
            }
        }

        @Override
        public void ping() {
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
        public void setConfig(Config config) {
            gameRegistry = new Registry(config, null);
        }

    }


}