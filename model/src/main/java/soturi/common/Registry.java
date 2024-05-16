package soturi.common;

import soturi.model.Config;
import soturi.model.DifficultyLvl;
import soturi.model.Enemy;
import soturi.model.EnemyType;
import soturi.model.EnemyTypeId;
import soturi.model.Item;
import soturi.model.ItemId;
import soturi.model.Loot;
import soturi.model.Polygon;
import soturi.model.PolygonId;
import soturi.model.Position;
import soturi.model.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Registry {
    private final long[] cumulativeXpForLvl;
    private final Map<ItemId, Item> itemMap;
    private final Map<EnemyTypeId, EnemyType> enemyMap;
    private final List<List<EnemyType>> enemiesPerLvl;
    private final List<Polygon> bannedAreas;
    private final Map<EnemyTypeId, List<Polygon>> spawnAreasPerType;

    private final Config config;
    private final GeoProvider geoProvider;
    private final Random rnd = new Random();

    public Registry(Config config, GeoProvider geoProvider) {
        this.config = config;
        this.geoProvider = geoProvider;

        itemMap = getAllItems().stream().collect(Collectors.toMap(Item::itemId, i -> i));
        enemyMap = getAllEnemyTypes().stream().collect(Collectors.toMap(EnemyType::typeId, e -> e));
        bannedAreas = getBanedAreaIds().stream().map(this::getPolygonById).collect(Collectors.toList());
        spawnAreasPerType = getAllEnemyTypes().stream().collect(Collectors.toMap(
            EnemyType::typeId,
            e -> e.spawnAreas().stream().map(this::getPolygonById).collect(Collectors.toList())
        ));

        cumulativeXpForLvl = new long[getMaxLvl() + 1];
        enemiesPerLvl = new ArrayList<>();
        enemiesPerLvl.add(List.of());

        for (int i = 1; i <= getMaxLvl(); ++i) {
            if (i > 1)
                cumulativeXpForLvl[i] = cumulativeXpForLvl[i - 1] + (long) config.xpToLvl().eval(i);
            int finalI = i;
            enemiesPerLvl.add(
                    getAllEnemyTypes().stream().filter(type -> type.lvlInRange(finalI)).collect(Collectors.toList())
            );
        }
        validate();
    }

    private void validate() {
        // validate resources
        for (Item item : getAllItems())
            validateResource(item.gfxName());
        for (EnemyType enemyType : getAllEnemyTypes())
            validateResource(enemyType.gfxName());

        // validate that PolygonIds are valid
        if (getGameArea() == null)
            throw new RuntimeException("game area not found");
        for (PolygonId banned : getBanedAreaIds())
            if (getPolygonById(banned) == null)
                throw new RuntimeException(banned + " polygon not found");
        for (EnemyType type : getAllEnemyTypes())
            for (PolygonId spawn : type.spawnAreas())
                if (getPolygonById(spawn) == null)
                    throw new RuntimeException(spawn + " polygon not found");

        // validate geoSplit
        if (getGameAreaSplitLvl() < 0) throw new RuntimeException("SplitLvl cannot be negative");
        if (getGameAreaSplitLvl() > 10) throw new RuntimeException("SplitLvl can be at most 10");

        // validate that there is enemy for each lvl
        for (int i = 1; i <= getMaxLvl(); ++i)
            if (getEnemyTypesPerLvl(i).isEmpty())
                throw new RuntimeException("there is no enemy for" + i + "lvl");

        // validate that each enemy has a spawn location
        for (EnemyType enemyType : getAllEnemyTypes())
            if (enemyType.spawnAreas().isEmpty())
                throw new RuntimeException(enemyType + " has no valid spawn location");

        // validate item loot
        for (EnemyType enemyType : getAllEnemyTypes())
            for (ItemId lootId : enemyType.lootList())
                if (getItemById(lootId) == null)
                    throw new RuntimeException(lootId + " item not found");

        // validate probabilities
        Function<Double, Boolean> okProbability = d -> 0 <= d && d <= 1;
        if (!okProbability.apply(getSpawnEnemyFailChance()))
            throw new RuntimeException("enemy spawn fail chance out of range");
        for (EnemyType enemyType : getAllEnemyTypes()) {
            if (!okProbability.apply(enemyType.failChance()))
                throw new RuntimeException("enemy fail chance out of range");
            if (!okProbability.apply(enemyType.lootChance()))
                throw new RuntimeException("loot chance out of range");
            if ((enemyType.lootChance() == 0) != enemyType.lootList().isEmpty())
                throw new RuntimeException("loot chance is 0 iff loot list is empty");
        }

        if (geoProvider != null)
            validateGeo();
    }

    private void validateGeo() {
        // validate polygons (geometry)
        for (PolygonId poly : config.areas().keySet())
            if (!geoProvider.isValid(getPolygonById(poly)))
                throw new RuntimeException(poly + " is invalid");
    }

    private void validateResource(String resource) {
        if (getClass().getClassLoader().getResource(resource) != null)
            return;
        System.err.println(
            "Resource: \"" + resource + "\" is missing. \n" +
            "(it should be placed inside classpath before compilation)\n"
        );
    }

    public <T> T getRandomElement(List<T> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    public int getFightingDistanceMaxInMeters() {
        return config.fightingDistanceMaxInMeters();
    }

    public int getMaxLvl() {
        return config.maxLvl();
    }

    /** Cumulative xp requirement for {@code lvl} (inclusive) */
    public long getXpForLvlCumulative(int lvl) {
        if (lvl < 0)
            return 0;
        if (lvl >= getMaxLvl() + 1)
            return Long.MAX_VALUE;
        return cumulativeXpForLvl[lvl];
    }

    /** Xp requirement to for from {@code lvl} to {@code lvl + 1} */
    public long getXpForNextLvl(int lvl) {
        if (lvl >= getMaxLvl())
            return Long.MAX_VALUE;
        return getXpForLvlCumulative(lvl + 1) - getXpForLvlCumulative(lvl);
    }

    /** Convert xp to lvl */
    public int getLvlFromXp(long xp) {
        int r = getMaxLvl() + 1, l = 1;
        while (r - l > 1) {
            int m = (l + r) / 2;
            if (xp >= getXpForLvlCumulative(m))
                l = m;
            else
                r = m;
        }
        return l;
    }

    public Polygon getPolygonById(PolygonId polygonId) {
        return config.areas().get(polygonId);
    }
    public PolygonId getGameAreaId() {
        return config.gameAreaId();
    }
    public Polygon getGameArea() {
        return getPolygonById(getGameAreaId());
    }
    public List<PolygonId> getBanedAreaIds() {
        return config.bannedAreas();
    }
    public List<Polygon> getBanedAreas() {
        return bannedAreas;
    }
    public int getGameAreaSplitLvl() {
        return config.gameAreaSplitLvl();
    }

    public boolean isInsideGameArea(Position position) {
        return geoProvider.isInside(getGameArea(), position) &&
               getBanedAreas().stream().noneMatch(area -> geoProvider.isInside(area, position));
    }
    public List<Polygon> getIntersectionWithGameArea(Polygon poly) {
        return geoProvider.intersect(poly, getGameArea());
    }

    public Item getItemById(ItemId itemId) {
        return itemMap.getOrDefault(itemId, Item.UNKNOWN);
    }
    public List<Item> getAllItems() {
        return config.items();
    }

    public EnemyType getEnemyTypeById(EnemyTypeId typeId) {
        return enemyMap.get(typeId);
    }
    public List<Polygon> getSpawnAreasForType(EnemyTypeId typeId) {
        return spawnAreasPerType.get(typeId);
    }
    public boolean isInsideSpawnAreaForType(EnemyTypeId typeId, Position position) {
        return getSpawnAreasForType(typeId).stream().anyMatch(poly -> geoProvider.isInside(poly, position));
    }
    public Position randomSpawnPointForType(EnemyTypeId typeId) {
        List<Polygon> polys = getSpawnAreasForType(typeId);
        Polygon poly = getRandomElement(polys);
        return geoProvider.randomPoint(poly);
    }

    public List<EnemyType> getAllEnemyTypes() {
        return config.enemyTypes();
    }
    public List<EnemyType> getEnemyTypesPerLvl(int lvl) {
        if (lvl < 1 || lvl > getMaxLvl())
            return List.of();
        return enemiesPerLvl.get(lvl);
    }
    public EnemyType getRandomEnemyTypeOfLvl(int lvl) {
        List<EnemyType> types = getEnemyTypesPerLvl(lvl);
        return getRandomElement(types);
    }

    public long getCityThreshold() {
        return config.cityThreshold();
    }
    public List<DifficultyLvl> getDifficulties() {
        return config.difficultyConfiguration();
    }

    public EnemyType getEnemyType(Enemy enemy) {
        return enemyMap.get(enemy.typeId());
    }
    public Statistics getEnemyStatistics(int lvl) {
        return config.statisticsEnemyPerLvl().mul(lvl).add(config.statisticsEnemyBase());
    }
    public Statistics getPlayerStatistics(int lvl) {
        return config.statisticsPlayerPerLvl().mul(lvl).add(config.statisticsPlayerBase());
    }
    public Loot getLootFor(Enemy enemy) {
        EnemyType type = getEnemyType(enemy);
        double xp = config.xpLoot().eval(enemy.lvl()) * type.xpFactor();
        List<ItemId> loot = rnd.nextDouble() < type.lootChance() ?
            List.of(getRandomElement(type.lootList())) : List.of();
        return new Loot((long) xp, loot);
    }

    public int getGiveFreeXpDelayInSeconds() {
        return config.giveFreeXpDelayInSeconds();
    }
    public long getGiveFreeXpAmount() {
        return config.giveFreeXpAmount();
    }
    public int getSpawnEnemyDelayInSeconds() {
        return config.spawnEnemyDelayInSeconds();
    }
    public double getSpawnEnemyFailChance() {
        return config.spawnEnemyFailChance();
    }
    public int getHealDelayInSeconds() {
        return config.healDelayInSeconds();
    }
    public double getHealFraction() {
        return config.healFraction();
    }
    public List<String> getCountryCodes() {
        return config.countryCodes();
    }
    public Config getConfig() {
        return config;
    }
}
