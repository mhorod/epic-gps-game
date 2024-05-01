package gps.tracker;

import java.util.HashMap;
import java.util.Map;

import gps.tracker.custom_overlays.EnemyOverlay;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Position;

public class EnemyList {
    private final Map<Enemy, EnemyOverlay> enemies;

    public EnemyList() {
        this.enemies = new HashMap<>();
    }

    public void addEnemy(Enemy enemy, EnemyOverlay overlay) {
        enemies.put(enemy, overlay);
    }

    public EnemyOverlay getOverlay(EnemyId enemy) {
        Enemy key = enemies.keySet().stream().filter(e -> e.enemyId().equals(enemy)).findFirst().orElse(null);
        return key == null ? null : enemies.get(key);
    }

    public void removeEnemy(EnemyId enemy) {
        Enemy key = enemies.keySet().stream().filter(e -> e.enemyId().equals(enemy)).findFirst().orElse(null);
        enemies.remove(key);
    }

    public Enemy getClosestEnemy(Position position) {
        Enemy closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;
        for (Enemy enemy : enemies.keySet()) {
            double distance = enemy.position().distance(position);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnemy = enemy;
            }
        }
        return closestEnemy;
    }
}
