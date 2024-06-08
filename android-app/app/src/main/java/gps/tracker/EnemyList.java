package gps.tracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gps.tracker.custom_overlays.EnemyOverlay;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Position;

public class EnemyList {
    private final Map<EnemyId, EnemyInstance> enemies;

    public EnemyList() {
        this.enemies = new HashMap<>();
    }

    public synchronized void addEnemy(@NonNull Enemy enemy, @Nullable EnemyOverlay overlay) {
        EnemyInstance instance = new EnemyInstance(enemy, overlay);

        enemies.put(enemy.enemyId(), instance);
    }

    public synchronized EnemyOverlay getOverlay(@NonNull EnemyId enemy) {
        if (!enemies.containsKey(enemy)) {
            return null;
        }

        EnemyInstance instance = enemies.get(enemy);

        return instance.overlay();
    }

    public synchronized void updateOverlayFor(@NonNull EnemyId enemy, @Nullable EnemyOverlay overlay) {
        if (!enemies.containsKey(enemy)) {
            return;
        }

        EnemyInstance oldInstance = enemies.get(enemy);
        enemies.remove(enemy);

        enemies.put(enemy, new EnemyInstance(oldInstance.enemy(), overlay));
    }

    public synchronized void removeEnemy(@NonNull EnemyId enemy) {
        enemies.remove(enemy);
    }

    public synchronized List<Enemy> getAllEnemies() {
        return enemies.values().stream()
                .map(EnemyInstance::enemy)
                .collect(Collectors.toList());
    }

    public synchronized List<Enemy> getEnemiesWithinRange(double range, Position position) {
        return enemies.values().stream().parallel()
                .filter(enemy -> enemy.enemy().position().distance(position) <= range)
                .map(EnemyInstance::enemy)
                .collect(Collectors.toList());
    }

    public synchronized void clear() {
        enemies.clear();
    }

    private record EnemyInstance(@NonNull Enemy enemy, @Nullable EnemyOverlay overlay) {
    }
}
