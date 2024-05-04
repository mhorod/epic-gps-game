package soturi.model;

// TODO remove the name and gfxName fields
public record Enemy(EnemyTypeId typeId, EnemyId enemyId, int lvl, Position position, String name, String gfxName) { }
