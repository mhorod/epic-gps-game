package soturi.server.geo;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import soturi.model.Area;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Position;
import soturi.model.RectangularArea;

import java.util.PriorityQueue;
import java.util.Random;

@UtilityClass
public class MonsterManagerUtility {
    public record IntPair(int i, int j) implements Comparable<IntPair> {
        @Override
        public int compareTo(@NonNull IntPair o) {
            if (i != o.i)
                return Integer.compare(i, o.i);
            return Integer.compare(j, o.j);
        }
    }
    public record IntTriple(int i, int j, int k) implements Comparable<IntTriple> {
        @Override
        public int compareTo(@NonNull IntTriple o) {
            if (i != o.i)
                return Integer.compare(i, o.i);
            if (j != o.j)
                return Integer.compare(j, o.j);
            return Integer.compare(k, o.k);
        }
    }

    public void markClose(int si, int sj, int di, int dj,
                                  RectangularArea[][] areas, Position from, double maxDist,
                                  int[][] marks, int filler) {

        for (int i = si; 0 <= i && i < areas.length; i += di) {
            int skips = 0;
            for (int j = sj; 0 <= j && j < areas[i].length && skips < 2; j += dj) {
                if (areas[i][j].getCenter().distance(from) > maxDist) {
                    ++skips;
                    continue;
                }
                marks[i][j] = filler;
            }
        }
    }

    // relaxes values assuming edges with weight 1
    public void dijkstra(int[][] w) {
        if (w.length != w[0].length)
            throw new RuntimeException("fuck you");
        final int n = w.length;

        PriorityQueue<IntTriple> pq = new PriorityQueue<>();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j)
                pq.add(new IntTriple(w[i][j], i, j));
        }
        while (pq.size() > 0) {
            IntTriple least = pq.poll();
            int i = least.j, j = least.k; // who needs structured binding

            for (int e = 0; e < 4; ++e) {
                int x = i + (e == 0 ? 1 : 0) - (e == 1 ? 1 : 0);
                int y = j + (e == 2 ? 1 : 0) - (e == 3 ? 1 : 0);
                if (y < 0 || y >= n || x < 0 || x >= n)
                    continue;
                if (w[i][j] + 1 < w[x][y]) {
                    w[x][y] = w[i][j] + 1;
                    pq.add(new IntTriple(w[x][y], x, y));
                }
            }
        }
    }

    public Enemy generateEnemy(Area area, EnemyId enemyId, Random rnd) {
        String name = "Mrówka";
        int lvl = 1 + (int) (rnd.nextDouble() * (3 + area.difficulty()));
        Position pos = area.dimensions().randomPosition(rnd);
        String gfxName = "gfx";

        return new Enemy(name, lvl, pos, enemyId, gfxName);
    }
}
