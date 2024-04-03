package gps.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.Enemy;
import model.Position;

public class EnemyTracker {

    private List<Enemy> enemies = new ArrayList<>();

    public synchronized void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public synchronized void removeEnemyFrom(Position position) {
        enemies.removeIf(enemy -> enemy.position().equals(position));
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public List<Enemy> getEnemiesWithinRange(Position position, double range) {
        return enemies.stream()
                .filter(enemy -> enemy.position().distance(position) <= range)
                .collect(Collectors.toList());
    }


}
