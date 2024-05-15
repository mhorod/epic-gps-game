import { Enemy, Player, Position } from "./model";

export type EnemiesAppear = {
  enemies: Enemy[];
};

export type EnemiesDisppear = {
  enemyIds: number[];
};

export type PlayerUpdate = {
  player: Player;
  position: Position | null;
};

export type PlayerDisappears = {
  playerName: string;
};
