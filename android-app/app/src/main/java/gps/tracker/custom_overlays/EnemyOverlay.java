package gps.tracker.custom_overlays;

import androidx.annotation.NonNull;

import org.osmdroid.views.MapView;

import gps.tracker.DrawableEnemy;
import soturi.model.EnemyId;

public class EnemyOverlay extends CustomOverlay {
    private final EnemyId id;

    public EnemyOverlay(MapView mapView, @NonNull DrawableEnemy enemy) {
        super(mapView, enemy.enemy().position(), enemy.drawable());

        this.id = enemy.enemy().enemyId();
    }

}
