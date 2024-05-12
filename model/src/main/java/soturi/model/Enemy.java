package soturi.model;

public record Enemy(EnemyTypeId typeId, EnemyId enemyId, int lvl, Position position) { }
