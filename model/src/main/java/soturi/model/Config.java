package soturi.model;

import lombok.With;

import java.util.List;
import java.util.Map;

@With
public record Config(
    Map<PolygonId, Polygon> areas,
    PolygonId gameAreaId,
    int gameAreaSplitLvl,
    List<PolygonId> bannedAreas,

    int fightingDistanceMaxInMeters,

    int maxLvl,
    XpFunction xpToLvl,
    XpFunction xpLoot,

    Statistics statisticsPlayerBase,
    Statistics statisticsPlayerPerLvl,

    Statistics statisticsEnemyBase,
    Statistics statisticsEnemyPerLvl,

    List<Item> items,
    List<EnemyType> enemyTypes,

    long cityThreshold,
    List<DifficultyLvl> difficultyConfiguration,
    List<String> countryCodes,

    int questDurationInSeconds,

    int giveFreeXpDelayInSeconds,
    long giveFreeXpAmount,

    int spawnEnemyDelayInSeconds,
    double spawnEnemyFailChance,

    int healDelayInSeconds,
    double healFraction
) { }
