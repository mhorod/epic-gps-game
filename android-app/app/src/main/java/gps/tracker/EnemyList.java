package gps.tracker;

import java.util.HashMap;
import java.util.Map;

import gps.tracker.custom_overlays.EnemyOverlay;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Position;

public class EnemyList {
    private final Map<EnemyId, EnemyInstance> enemies;

    public EnemyList() {
        this.enemies = new HashMap<>();
    }

    public void addEnemy(Enemy enemy, EnemyOverlay overlay) {
        EnemyInstance instance = new EnemyInstance(enemy, overlay);

        enemies.put(enemy.enemyId(), instance);
    }

    public EnemyOverlay getOverlay(EnemyId enemy) {
        if (!enemies.containsKey(enemy)) {
            return null;
        }

        EnemyInstance instance = enemies.get(enemy);

        return instance.overlay();
    }

    public void removeEnemy(EnemyId enemy) {
        enemies.remove(enemy);
    }

    public Enemy getClosestEnemy(Position position) {
        Enemy closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;
        for (EnemyInstance instance : enemies.values()) {
            Enemy enemy = instance.enemy();
            double distance = enemy.position().distance(position);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnemy = enemy;
            }
        }
        return closestEnemy;
    }

    private record EnemyInstance(Enemy enemy, EnemyOverlay overlay) {
    }
}
