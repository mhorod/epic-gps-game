package gps.tracker;

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

    public synchronized void addEnemy(Enemy enemy, EnemyOverlay overlay) {
        EnemyInstance instance = new EnemyInstance(enemy, overlay);

        enemies.put(enemy.enemyId(), instance);
    }

    public synchronized EnemyOverlay getOverlay(EnemyId enemy) {
        if (!enemies.containsKey(enemy)) {
            return null;
        }

        EnemyInstance instance = enemies.get(enemy);

        return instance.overlay();
    }

    public synchronized void removeEnemy(EnemyId enemy) {
        enemies.remove(enemy);
    }

    public synchronized Enemy getClosestEnemy(Position position) {
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

    public synchronized List<EnemyOverlay> getAllEnemyOverlays() {
        return enemies.values().stream()
                .map(EnemyInstance::overlay)
                .collect(Collectors.toList());
    }

    public synchronized List<Enemy> getAllEnemies() {
        return enemies.values().stream()
                .map(EnemyInstance::enemy)
                .collect(Collectors.toList());
    }

    public synchronized void clear() {
        enemies.clear();
    }

    public synchronized boolean empty() {
        return enemies.isEmpty();
    }

    private record EnemyInstance(Enemy enemy, EnemyOverlay overlay) {
    }
}
