package soturi.content;

import soturi.model.Enemy;
import soturi.model.EnemyType;
import soturi.model.EnemyTypeId;
import soturi.model.Loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnemyRegistry {
    private final Map<EnemyTypeId, EnemyType> enemiesById = new LinkedHashMap<>();
    private final List<EnemyType> enemyList = new ArrayList<>();

    private final List<EnemyType> normalEnemyList = new ArrayList<>();
    private final List<EnemyType> bossList = new ArrayList<>();

    private void registerEnemy(EnemyType enemyType) {
        if (enemiesById.put(enemyType.typeId(), enemyType) != null)
            throw new RuntimeException();
        enemyList.add(enemyType);

        if (enemyType.isBoss())
            bossList.add(enemyType);
        else
            normalEnemyList.add(enemyType);

        SanityChecker.checkEncodingAndReport();
        SanityChecker.checkResourceAndReport("static/" + enemyType.gfxName());
    }

    public EnemyType getEnemyTypeById(EnemyTypeId typeId) {
        return enemiesById.get(typeId);
    }
    public EnemyType getEnemyType(Enemy enemy) {
        return getEnemyTypeById(enemy.typeId());
    }
    public long getEnemyAttack(Enemy enemy) {
        return enemy.lvl() * 40L;
    }
    public long getEnemyDefense(Enemy enemy) {
        return enemy.lvl() * 20L;
    }
    public long getEnemyHp(Enemy enemy) {
        return enemy.lvl() * 75L;
    }

    public List<EnemyType> getAllEnemyTypes() {
        return Collections.unmodifiableList(enemyList);
    }
    public List<EnemyType> getNormalEnemyTypes() {
        return Collections.unmodifiableList(normalEnemyList);
    }
    public List<EnemyType> getAllBossTypes() {
        return Collections.unmodifiableList(bossList);
    }

    private final GameRegistry gameRegistry = new GameRegistry();
    public Loot lootFor(Enemy enemy) {
        EnemyType type = getEnemyType(enemy);
        long xp = (long) (gameRegistry.getXpForNextLvl(enemy.lvl()) * type.xpFactor() / Math.sqrt(2 * enemy.lvl()));
        return new Loot(xp, List.of()); // TODO items
    }

    public EnemyRegistry() {
        registerEnemy(new EnemyType(
            new EnemyTypeId(0),
            SanityChecker.fixEnc("Smok Wawelski"),
            "assets/enemies/1 Portraits/Icons_34.png",
            50,
            50,
            1,
            true,
            2.0,
            GeoRegistry.WAWEL,
            120,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(1),
            SanityChecker.fixEnc("Dzik"),
            "assets/enemies/1 Portraits/Icons_30.png",
            15,
            20,
            2,
            true,
            1.5,
            GeoRegistry.BOARS_SKOSNA,
            20,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(3),
            SanityChecker.fixEnc("Dzik"),
            "assets/enemies/1 Portraits/Icons_30.png",
            10,
            12,
            5,
            true,
            1.5,
            GeoRegistry.BOARS_ZAKRZOWEK,
            15,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(2),
            SanityChecker.fixEnc("Student TCS"),
            "assets/enemies/1 Portraits/Icons_17.png",
            5,
            10,
            1,
            true,
            1.5,
            GeoRegistry.TCS_UJ,
            10,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(10),
            SanityChecker.fixEnc("Żaba"),
            "assets/enemies/1 Portraits/Icons_05.png",
            1,
            3,
            Integer.MAX_VALUE,
            false,
            1.0,
            GeoRegistry.EARTH,
            1,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(11),
            SanityChecker.fixEnc("Żółw"),
            "assets/enemies/1 Portraits/Icons_39.png",
            3,
            10,
            Integer.MAX_VALUE,
            false,
            1.0,
            GeoRegistry.EARTH,
            1,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(12),
            SanityChecker.fixEnc("Pająk"),
            "assets/enemies/1 Portraits/Icons_22.png",
            8,
            1000,
            Integer.MAX_VALUE,
            false,
            1.0,
            GeoRegistry.EARTH,
            1,
            List.of(),
            0.0
        ));
        registerEnemy(new EnemyType(
            new EnemyTypeId(13),
            SanityChecker.fixEnc("Niedźwiedź"),
            "assets/enemies/1 Portraits/Icons_21.png",
            12,
            1000,
            Integer.MAX_VALUE,
            false,
            1.0,
            GeoRegistry.EARTH,
            1,
            List.of(),
            0.0
        ));
    }
}
