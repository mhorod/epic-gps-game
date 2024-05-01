package gps.tracker.custom_overlays;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import gps.tracker.DrawableEnemy;
import soturi.model.EnemyId;

public class EnemyOverlay extends CustomOverlay {
    private final EnemyId id;
    private Runnable onSingleTapConfirmed = () -> {
    };

    public EnemyOverlay(MapView mapView, @NonNull DrawableEnemy enemy) {
        super(mapView, enemy.enemy().position(), enemy.drawable());

        this.id = enemy.enemy().enemyId();
    }

    private boolean amITapped(@NonNull MotionEvent e) {
        BoundingBox myBoundingBox = mBounds;
        System.out.println("This" + this);
        System.out.println("EnemyOverlay.amITapped: myBoundingBox = " + myBoundingBox);
        System.out.println("EnemyOverlay.amITapped: e.getX() = " + e.getX());
        System.out.println("EnemyOverlay.amITapped: e.getY() = " + e.getY());
        return myBoundingBox.contains(e.getX(), e.getY());
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        super.onSingleTapConfirmed(e, mapView);

        if (amITapped(e)) {
            onSingleTapConfirmed.run();
            return true;
        } else {
            return false;
        }
    }

    public void setOnSingleTapConfirmed(Runnable onSingleTapConfirmed) {
        this.onSingleTapConfirmed = onSingleTapConfirmed;
    }

    public EnemyId getId() {
        return id;
    }
}
