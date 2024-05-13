package soturi.model;

import java.util.List;

public record EnemyType(
    EnemyTypeId typeId,
    String name,
    String gfxName,
    int minLvl,
    int maxLvl,
    double statisticsMul,
    int totalCap,
    double xpFactor,
    List<PolygonId> spawnAreas,
    boolean ignoreAreaDifficulty,
    boolean ignoreAreaCap,
    double failChance,
    List<ItemId> lootList,
    double lootChance
) {
    public boolean lvlInRange(int lvl) {
        return minLvl <= lvl && lvl <= maxLvl;
    }
}
