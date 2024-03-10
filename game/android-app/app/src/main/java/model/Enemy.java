package model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record Enemy(String name, int lvl, Position position, EnemyId enemyId, String gfxName) { }
