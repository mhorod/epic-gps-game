import { Enemy, EnemyId, Player, Position } from "./model";

export type EnemyAppears = {
  enemy: Enemy;
};

export type EnemiesAppear = {
  enemies: Enemy[];
};

export type EnemyDisappears = {
  enemyId: EnemyId;
};

export type EnemiesDisppear = {
  enemies: EnemyId[];
};

export type PlayerUpdate = {
  player: Player;
  position: Position | null;
};

export type PlayerDisappears = {
  playerName: string;
};
