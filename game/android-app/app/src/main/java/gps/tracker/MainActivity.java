package gps.tracker;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import gps.tracker.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private WebSocketClient webSocketClient = new WebSocketClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        requestPermissions();
    }

    private void requestPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> postRequestPermissions()
                );
        locationPermissionRequest.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
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

        locationListener = this::processLocationUnchecked;
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

    private void processLocationUnchecked(Location location) {
        Log.e("main_activity", location.toString());
        new Thread(() -> doProcessLocationUnchecked(location)).start();
    }

    private void doProcessLocationUnchecked(Location location) {
        try {
            doProcessLocation(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doProcessLocation(Location location) throws IOException {
        Log.e("main_activity", "doProcessLocation()");
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String loc = lat + "," + lng;
        URL url = new URL("http://52.158.44.176:8080/v1/push-list?str="+loc);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("str", "xdfromandroid");
        urlConnection.connect();
        Log.e("main_activity", "ret code is " + urlConnection.getResponseCode());
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
}