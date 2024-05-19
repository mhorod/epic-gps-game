package soturi.server.geo;

import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableInt;
import soturi.common.Registry;
import soturi.model.DifficultyLvl;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyType;
import soturi.model.EnemyTypeId;
import soturi.model.Polygon;
import soturi.model.PolygonWithDifficulty;
import soturi.model.Position;
import soturi.model.Rectangle;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class MonsterManager {
    private final Registry registry;
    private final Supplier<EnemyId> enemyIdSupplier;

    private final List<PolygonWithDifficulty> areasWithDifficulties = new ArrayList<>();
    private final Region[] regions;

    private final int[][] regionIdOf;
    private final Rectangle fullEnvelope;

    private final Map<EnemyId, Enemy> enemies = new LinkedHashMap<>();
    private final Map<EnemyTypeId, Set<EnemyId>> enemiesPerType = new LinkedHashMap<>();

    record Region(Rectangle rectangle, List<EnemyId> enemiesHere, int difficulty, MutableInt capLeft) {
        Region(Rectangle rectangle, int difficulty, int cap) {
            this(rectangle, new ArrayList<>(), difficulty, new MutableInt(cap));
        }
    }

    private int latitudeIdOf(double latitude) {
        int n = 1 << registry.getGameAreaSplitLvl();
        double envLatW = fullEnvelope.upperLatitude() - fullEnvelope.lowerLatitude();
        double dx = (latitude - fullEnvelope.lowerLatitude()) / envLatW * n;
        return Math.max(0, Math.min((int) dx, n - 1));
    }
    private int longitudeIdOf(double longitude) {
        int n = 1 << registry.getGameAreaSplitLvl();
        double envLonW = fullEnvelope.upperLongitude() - fullEnvelope.lowerLongitude();
        double dy = (longitude - fullEnvelope.lowerLongitude()) / envLonW * n;
        return Math.max(0, Math.min((int) dy, n - 1));
    }
    private int getRegionIdOf(Position position) {
        return regionIdOf[latitudeIdOf(position.latitude())][longitudeIdOf(position.longitude())];
    }

    public List<PolygonWithDifficulty> getAreas() {
        return areasWithDifficulties;
    }

    private class RegionGenerator {
        final int n = 1 << registry.getGameAreaSplitLvl();
        final int[][] difficulty = new int[n][n];

        final List<Region> generatedRegions = new ArrayList<>();
        final int[][] generatedRegionIdOf = new int[n][n];

        @With
        record RegionInfo(int latL, int latR, int lonL, int lonR, Rectangle rectangle, int difficulty, int cap) { }

        void registerRegion(RegionInfo info) {
            if (info == null)
                return;
            Region region = new Region(info.rectangle, info.difficulty, info.cap);

            for (int i = info.latL; i < info.latR; ++i)
                for (int j = info.lonL; j < info.lonR; ++j)
                    generatedRegionIdOf[i][j] = generatedRegions.size();
            generatedRegions.add(region);
        }

        RegionInfo recursive(int latL, int latR, int lonL, int lonR, Rectangle rectangle) {
            RegionInfo info = new RegionInfo(latL, latR, lonL, lonR, rectangle, difficulty[latL][lonL], 1);

            if (latR - latL == 1)
                return info;

            Rectangle[][] subRectangles = rectangle.kSplit(2);
            int latM = (latL + latR) / 2;
            int lonM = (lonL + lonR) / 2;

            RegionInfo[] infos = {
                recursive(latL, latM, lonL, lonM, subRectangles[0][0]),
                recursive(latL, latM, lonM, lonR, subRectangles[0][1]),
                recursive(latM, latR, lonL, lonM, subRectangles[1][0]),
                recursive(latM, latR, lonM, lonR, subRectangles[1][1])
            };

            for (RegionInfo subInfo : infos) {
                if (subInfo == null || subInfo.difficulty != info.difficulty || info.difficulty == 0) {
                    Arrays.stream(infos).forEach(this::registerRegion);
                    return null;
                }
            }

            return info.withCap(infos[0].cap * 3);
        }

        void mark(Position position, int value, double radius, int di, int dj) {
            int startI = latitudeIdOf(position.latitude());
            int startJ = longitudeIdOf(position.longitude());

            outerLoop:
            for (int i = startI; 0 <= i && i < n; i += di) {
                for (int j = startJ; 0 <= j && j < n; j += dj) {
                    Position center = fullEnvelope.proportionalPosition((i + 0.5) / n, (j + 0.5) / n);
                    if (position.distance(center) >= radius) {
                        if (j == startJ)
                            break outerLoop;
                        break;
                    }
                    difficulty[i][j] = Math.min(difficulty[i][j], value);
                }
            }
        }

        void processCity(City city) {
            if (city.population() < registry.getCityThreshold())
                return;

            double scale = Math.log10(city.population()) / 6; // city 1M <=> scale == 1
            List<DifficultyLvl> difficulties = registry.getDifficulties();

            for (int lvl = 0; lvl < difficulties.size() - 1; ++lvl) {
                double radius = difficulties.get(lvl).radiusInMeters() * scale;
                mark(city.position(), lvl, radius, -1, -1);
                mark(city.position(), lvl, radius, -1, +1);
                mark(city.position(), lvl, radius, +1, -1);
                mark(city.position(), lvl, radius, +1, +1);
            }
        }

        RegionGenerator(List<City> cities) {
            if ((n & (n - 1)) > 0)
                throw new RuntimeException("regionIdOf.length has to be a power of 2");
            for (int[] row : difficulty)
                Arrays.fill(row, registry.getDifficulties().size() - 1);

            cities.forEach(this::processCity);
            registerRegion(recursive(0, n, 0, n, fullEnvelope));
            log.info("Generated {} regions", generatedRegions.size());
        }
    }

    public MonsterManager(CityProvider cityProvider, Registry registry, Supplier<EnemyId> enemyIdSupplier) {
        this.registry = registry;
        this.enemyIdSupplier = enemyIdSupplier;

        for (EnemyType type : registry.getAllEnemyTypes())
            enemiesPerType.put(type.typeId(), new LinkedHashSet<>());

        fullEnvelope = Rectangle.envelopeOf(registry.getGameArea());

        RegionGenerator generator = new RegionGenerator(cityProvider.getCities());
        regionIdOf = generator.generatedRegionIdOf;
        regions = generator.generatedRegions.toArray(Region[]::new);

        for (Region region : regions) {
            List<Polygon> polys = registry.getIntersectionWithGameArea(region.rectangle().asPolygon());
            polys.forEach(poly -> areasWithDifficulties.add(new PolygonWithDifficulty(poly, region.difficulty)));
        }
    }

    public void registerEnemy(Enemy enemy) {
        EnemyId enemyId = enemy.enemyId();
        EnemyType type = registry.getEnemyType(enemy);
        Region region = regions[getRegionIdOf(enemy.position())];

        if (enemies.containsKey(enemyId))
            throw new RuntimeException();

        enemies.put(enemyId, enemy);
        enemiesPerType.get(enemy.typeId()).add(enemyId);
        region.enemiesHere.add(enemyId);

        if (!type.ignoreAreaCap())
            region.capLeft.decrement();
    }
    public void unregisterEnemy(EnemyId enemyId) {
        Enemy enemy = enemies.remove(enemyId);
        EnemyType type = registry.getEnemyType(enemy);
        Region region = regions[getRegionIdOf(enemy.position())];

        if (!enemiesPerType.get(enemy.typeId()).remove(enemyId))
            throw new RuntimeException();
        if (!region.enemiesHere.remove(enemyId))
            throw new RuntimeException();

        if (!type.ignoreAreaCap())
            region.capLeft.increment();
    }

    public List<Enemy> getAllEnemies() {
        return enemies.values().stream().toList();
    }
    public Map<EnemyId, Enemy> getEnemyMap() {
        return Collections.unmodifiableMap(enemies);
    }

    private class EnemyGenerator {
        final Random rnd = new Random();
        final List<Enemy> generated = new ArrayList<>();
        final Map<EnemyTypeId, Integer> capLeftByType = new HashMap<>();
        final int[] capLeftByRegion;

        int minLvl(Region region) {
            return registry.getDifficulties().get(region.difficulty).minLvl();
        }
        int maxLvl(Region region) {
            return registry.getDifficulties().get(region.difficulty).maxLvl();
        }
        boolean lvlInRange(Region region, int lvl) {
            return minLvl(region) <= lvl && lvl <= maxLvl(region);
        }

        void tryAdd(Enemy enemy) {
            EnemyType type = registry.getEnemyType(enemy);
            int typeCapLeft = capLeftByType.get(type.typeId());
            int regionId = getRegionIdOf(enemy.position());

            if (typeCapLeft <= 0)
                return;
            if (!type.ignoreAreaCap() && capLeftByRegion[regionId] <= 0)
                return;
            if (!type.ignoreAreaDifficulty() && !lvlInRange(regions[regionId], enemy.lvl()))
                return;
            if (!registry.isInsideGameArea(enemy.position()))
                return;
            if (!registry.isInsideSpawnAreaForType(enemy.typeId(), enemy.position()))
                return;

            generated.add(enemy);
            capLeftByType.put(type.typeId(), typeCapLeft - 1);
            if (!type.ignoreAreaCap())
                capLeftByRegion[regionId]--;
        }

        EnemyGenerator() {
            capLeftByRegion = Arrays.stream(regions).mapToInt(r -> r.capLeft.intValue()).toArray();
            for (EnemyType type : registry.getAllEnemyTypes()) {
                int currently = enemiesPerType.get(type.typeId()).size();
                capLeftByType.put(type.typeId(), type.totalCap() < 0 ? Integer.MAX_VALUE : type.totalCap() - currently);
            }

            // area based algo
            List<Region> shuffledRegions = new ArrayList<>(Arrays.asList(regions));
            Collections.shuffle(shuffledRegions);
            for (Region region : shuffledRegions) {
                if (rnd.nextDouble() < registry.getSpawnEnemyFailChance() || generated.size() >= 2000)
                    continue;
                Position position = region.rectangle.randomPosition(rnd);
                int lvl = rnd.nextInt(minLvl(region), maxLvl(region) + 1);
                EnemyTypeId typeId = registry.getRandomEnemyTypeOfLvl(lvl).typeId();
                EnemyId enemyId = enemyIdSupplier.get();
                tryAdd(new Enemy(typeId, enemyId, lvl, position));
            }

            // type based algo
            for (EnemyType type : registry.getAllEnemyTypes()) {
                if (rnd.nextDouble() < type.failChance())
                    continue;
                Position position = registry.randomSpawnPointForType(type.typeId());
                int lvl = rnd.nextInt(type.minLvl(), type.maxLvl() + 1);
                EnemyId enemyId = enemyIdSupplier.get();
                tryAdd(new Enemy(type.typeId(), enemyId, lvl, position));
            }
        }
    }

    /** this generates valid candidates to register, but does NOT actually register them */
    public List<Enemy> generateEnemies() {
        return new EnemyGenerator().generated;
    }
}
