package soturi.server.geo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import soturi.model.Area;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Position;
import soturi.model.RectangularArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static soturi.server.geo.MonsterManagerUtility.*;

@Slf4j
@Component
public final class MonsterManager {
    private final CityProvider cityProvider;

    private List<RectangularArea[][]> splits;
    private int maxSplit;
    private List<Area> areas;

    private List<EnemyId>[][] enemiesPerRegion;
    private final Map<EnemyId, Enemy> enemies = new LinkedHashMap<>();

    public RectangularArea fullArea() {
        return splits.get(0)[0][0];
    }
    public List<Area> getAreas() {
        return Collections.unmodifiableList(areas);
    }

    public IntPair indexOf(Position pos, int depth) { // depth is from [0, maxSplit)
        if (!fullArea().isInside(pos))
            throw new RuntimeException();
        if (depth < 0 || depth >= maxSplit)
            throw new RuntimeException("invalid depth argument");

        IntPair r = new IntPair(0, 0);

        for (int d = 1; d <= depth; ++d) {
            double best = Double.MAX_VALUE;
            IntPair best_r = null;

            for (int next_i = 2 * r.i(); next_i < 2 * r.i() + 2; ++next_i) {
                for (int next_j = 2 * r.j(); next_j < 2 * r.j() + 2; ++next_j) {
                    double here = splits.get(d)[next_i][next_j].getCenter().distance(pos);
                    if (here < best) {
                        best = here;
                        best_r = new IntPair(next_i, next_j);
                    }
                }
            }

            r = best_r;
        }
        return r;
    }
    public IntPair indexOf(Position pos) {
        return indexOf(pos, maxSplit - 1);
    }

    public void restart(Environment env) {
        log.info("GeoManager::restart()");
        if (!enemies.isEmpty())
            throw new RuntimeException("remove all enemies first");

        maxSplit = env.getRequiredProperty("geo.max-split", int.class);

        double minLatitude = env.getRequiredProperty("geo.min-latitude", double.class);
        double maxLatitude = env.getRequiredProperty("geo.max-latitude", double.class);
        double minLongitude = env.getRequiredProperty("geo.min-longitude", double.class);
        double maxLongitude = env.getRequiredProperty("geo.max-longitude", double.class);
        RectangularArea fullArea = new RectangularArea(minLatitude, maxLatitude, minLongitude, maxLongitude);

        splits = new ArrayList<>();
        for (int i = 0; i < maxSplit; ++i)
            splits.add(fullArea.kSplit(1 << i));

        int n = 1 << (maxSplit-1);
        int[][] marks = new int[n][n];
        for (int[] row : marks)
            Arrays.fill(row, (int) 1e9);

        for (City city : cityProvider.getCities()) {
            if (!fullArea.isInside(city.position()))
                continue;
            IntPair ij = indexOf(city.position());
            int i = ij.i(), j = ij.j();

            //   0 population -> 0km
            //  1k population -> 1km
            // 1kk population -> 2km
            double distance_mx = 1000 * (Math.log10(city.population()) / 3);

            markClose(i, j, +1, +1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(i, j, +1, -1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(i, j, -1, +1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(i, j, -1, -1, splits.getLast(), city.position(), distance_mx, marks, 1);
            marks[i][j] = 1;
        }
        dijkstra(marks);

        log.info("n**2: {}", n*n);

        areas = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                marks[i][j] = (int) Math.sqrt(marks[i][j] + 0.5);

                areas.add(new Area(splits.getLast()[i][j], marks[i][j]));
            }
        }

        enemies.clear();
        enemiesPerRegion = new List[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j)
                enemiesPerRegion[i][j] = new ArrayList<>();
        }
    }

    public MonsterManager(CityProvider cityProvider, Environment env) {
        this.cityProvider = cityProvider;
        restart(env);
    }

    long nextEnemyIdLong = 0;
    public EnemyId nextEnemyId() {
        return new EnemyId(nextEnemyIdLong++);
    }
    public void registerEnemy(Enemy enemy) {
        EnemyId enemyId = enemy.enemyId();

        if (enemies.containsKey(enemyId))
            throw new RuntimeException();

        enemies.put(enemyId, enemy);

        IntPair ij = indexOf(enemy.position());
        int i = ij.i(), j = ij.j();
        enemiesPerRegion[i][j].add(enemyId);
    }
    public void unregisterEnemy(EnemyId enemyId) {
        Enemy enemy = enemies.remove(enemyId);
        if (enemy == null)
            throw new RuntimeException();

        IntPair ij = indexOf(enemy.position());
        int i = ij.i(), j = ij.j();

        if (!enemiesPerRegion[i][j].remove(enemyId))
            throw new RuntimeException();
    }

    public List<Enemy> getAllEnemies() {
        return enemies.values().stream().toList();
    }
    public Map<EnemyId, Enemy> getEnemyMap() {
        return Collections.unmodifiableMap(enemies);
    }

    private final Random rnd = new Random();
    /* this generates valid candidates to register, but does NOT actually register them */
    public List<Enemy> generateEnemies() {
        List<Enemy> returnList = new ArrayList<>();

        for (Area area : areas) {
            IntPair ij = indexOf(area.dimensions().getCenter()); // inefficient af
            int i = ij.i(), j = ij.j();

            int cap = Math.max(0, 5 - area.difficulty());
            int curr = enemiesPerRegion[i][j].size();

            double failProbability = 1.0 * curr / cap;
            if (rnd.nextDouble() < failProbability)
                continue;

            returnList.add(generateEnemy(area, nextEnemyId(), rnd));
        }

        return returnList;
    }

}
