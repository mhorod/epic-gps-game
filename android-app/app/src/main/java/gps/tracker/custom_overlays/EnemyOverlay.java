package gps.tracker.custom_overlays;

import android.app.AlertDialog;
import android.location.Location;

import androidx.annotation.NonNull;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.function.Consumer;

import gps.tracker.DrawableEnemy;
import gps.tracker.MainActivity;
import soturi.model.Enemy;
import soturi.model.Position;

public class EnemyOverlay extends CustomOverlay {
    private final Enemy enemy;
    private final MainActivity mainActivity;
    private final MapView mapView;
    private final Consumer<Enemy> onAttack;

    public EnemyOverlay(MapView mapView, @NonNull DrawableEnemy enemy, MainActivity mainActivity, Consumer<Enemy> onAttack) {
        super(mapView, enemy.enemy().position(), enemy.drawable());

        this.mainActivity = mainActivity;
        this.mapView = mapView;
        this.enemy = enemy.enemy();
        this.onAttack = onAttack;
    }

    @Override
    public boolean onMarkerClickDefault(Marker marker, MapView mapView) {
        if (marker == this) {
            handleBeingClicked();
            return true;
        }
        return false;
    }

    private void handleBeingClicked() {
        if (mainActivity.getLastLocation() == null) {
            return;
        }

        Location userLocation = mainActivity.getLastLocation();
        Position userPosition = new Position(userLocation.getLatitude(), userLocation.getLongitude());
        Position enemyPosition = new Position(this.getPosition().getLatitude(), this.getPosition().getLongitude());

        boolean canAttack = userPosition.distance(enemyPosition) < mainActivity.gameRegistry.getFightingDistanceMaxInMeters();

        mainActivity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

            String name = mainActivity.gameRegistry.getEnemyType(this.enemy).name();
            builder.setTitle("Enemy: " + name + " lvl " + enemy.lvl());

            if (canAttack) {
                builder.setMessage("Proceed with an attack?");
                builder.setPositiveButton("OK", (dialog, id) -> {
                    onAttack.accept(enemy);
                    dialog.dismiss();
                });
                builder.setNegativeButton("Nope", (dialog, id) -> dialog.dismiss());
            } else {
                builder.setMessage("You are too far away to attack this enemy!");
                builder.setPositiveButton("Quite the predicament", (dialog, id) -> dialog.dismiss());
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }


}
