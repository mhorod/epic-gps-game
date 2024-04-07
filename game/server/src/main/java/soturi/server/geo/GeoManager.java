package soturi.server.geo;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import soturi.model.Area;
import soturi.model.Position;
import soturi.model.RectangularArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

@Slf4j
@Component
public final class GeoManager {
    private List<RectangularArea[][]> splits = new ArrayList<>();
    private int maxSplit;
    private List<Area> areas = new ArrayList<>();

    public RectangularArea fullArea() {
        return splits.get(0)[0][0];
    }
    public List<Area> getAreas() {
        return Collections.unmodifiableList(areas);
    }

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

    public IntPair indexOf(Position pos, int depth) { // depth is from [0, maxSplit)
        if (!fullArea().isInside(pos))
            throw new RuntimeException();
        if (depth < 0 || depth >= maxSplit)
            throw new RuntimeException("invalid depth argument");

        IntPair r = new IntPair(0, 0);

        for (int d = 1; d <= depth; ++d) {
            double best = Double.MAX_VALUE;
            IntPair best_r = null;

            for (int next_i = 2 * r.i; next_i < 2 * r.i + 2; ++next_i) {
                for (int next_j = 2 * r.j; next_j < 2 * r.j + 2; ++next_j) {
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

    private static void markClose(int si, int sj, int di, int dj,
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
    public static void dijkstra(int[][] w) {
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

    public GeoManager(CityProvider cityProvider, Environment env) {
        log.info("GeoManager()");

        maxSplit = env.getRequiredProperty("geo.max-split", int.class);

        double minLatitude = env.getRequiredProperty("geo.min-latitude", double.class);
        double maxLatitude = env.getRequiredProperty("geo.max-latitude", double.class);
        double minLongitude = env.getRequiredProperty("geo.min-longitude", double.class);
        double maxLongitude = env.getRequiredProperty("geo.max-longitude", double.class);
        RectangularArea fullArea = new RectangularArea(minLatitude, maxLatitude, minLongitude, maxLongitude);

        for (int i = 0; i < maxSplit; ++i)
            splits.add(fullArea.kSplit(1 << i));

        log.info("{}", splits);
        int[][] marks = new int[1 << (maxSplit-1)][1 << (maxSplit-1)];
        for (int[] row : marks)
            Arrays.fill(row, (int) 1e9);

        for (City city : cityProvider.getCities()) {
            if (!fullArea.isInside(city.position()))
                continue;
            IntPair ij = indexOf(city.position());

            //   0 population -> 0km
            //  1k population -> 1km
            // 1kk population -> 2km
            double distance_mx = 1000 * (Math.log10(city.population()) / 3);

            markClose(ij.i, ij.j, +1, +1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(ij.i, ij.j, +1, -1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(ij.i, ij.j, -1, +1, splits.getLast(), city.position(), distance_mx, marks, 1);
            markClose(ij.i, ij.j, -1, -1, splits.getLast(), city.position(), distance_mx, marks, 1);
            marks[ij.i][ij.j] = 1;
        }
        dijkstra(marks);

        log.info("marks.length**2: {}", marks.length*marks.length);

        for (int i = 0; i < marks.length; ++i) {
            for (int j = 0; j < marks.length; ++j) {
                marks[i][j] = (int) Math.sqrt(marks[i][j] + 0.5);

                areas.add(new Area(splits.getLast()[i][j], marks[i][j]));
            }
        }
    }
}
