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
import android.view.View;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import gps.tracker.databinding.ActivityMainBinding;
import gps.tracker.simple_listeners.Notifier;
import lombok.SneakyThrows;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.MessageToClientHandler;

public class MainActivity extends AppCompatActivity {

    public final Notifier locationChangeRequestNotifier = new Notifier();
    private final List<LocationListener> sublisteners = new ArrayList<>();
    private WebSocketClient webSocketClient;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location lastLocation;
    private Consumer<Enemy> enemyAppearsConsumer = (Enemy e) -> {
    };
    private Consumer<EnemyId> enemyDisappearsConsumer = (EnemyId eid) -> {
    };
    private FragmentManager fragmentManager;

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


        binding.findMeButton.setOnClickListener(view -> {
            locationChangeRequestNotifier.notifyListeners();
        });
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

    public void login(String userName, String userPassword) {
        webSocketClient = new WebSocketClient(new MainActivityHandler(), userName, userPassword);
    }

    public boolean loggedIn() {
        return webSocketClient != null;
    }

    public void hideLocationKey() {
        binding.findMeButton.setVisibility(View.GONE);
    }

    public void showLocationKey() {
        binding.findMeButton.setVisibility(View.VISIBLE);
    }

    class MainActivityHandler implements MessageToClientHandler {

        @Override
        public void disconnect() {

        }

        @Override
        public void enemyAppears(Enemy enemy) {
            enemyAppearsConsumer.accept(enemy);
        }

        @Override
        public void enemyDisappears(EnemyId enemyId) {
            enemyDisappearsConsumer.accept(enemyId);
        }

        @Override
        public void error(String error) {
            System.err.println(error);
        }

        @Override
        public void fightResult(Result result, EnemyId enemyId) {
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                if (result == Result.WON) {
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

    }


}