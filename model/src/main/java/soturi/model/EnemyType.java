package soturi.model;

import lombok.Builder;

import java.util.List;

@Builder
public record EnemyType(
    EnemyTypeId typeId,
    String name,
    String gfxName,
    int minLvl,
    int maxLvl,
    int cap,
    boolean isBoss,
    double xpFactor,
    RectangularArea allowedArea,
    int freqSuccess,
    List<ItemId> lootList,
    double lootChance
) {
    public boolean lvlInRange(int lvl) {
        return minLvl <= lvl && lvl <= maxLvl;
    }
    public Enemy createEnemy(EnemyId enemyId, int lvl, Position position) {
        if (!lvlInRange(lvl))
            throw new RuntimeException();
        return new Enemy(
            typeId,
            enemyId,
            lvl,
            position,
            name,
            gfxName
        );
    }
}
