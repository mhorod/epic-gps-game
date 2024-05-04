export type Position = {
  latitude: number;
  longitude: number;
};

export type EnemyId = {
  id: string;
};

export type Enemy = {
  name: string;
  lvl: number;
  position: Position;
  enemyId: EnemyId;
  gfxName: string;
};

export type ItemId = {
  id: string;
};

export type Player = {
  name: string;
  lvl: number;
  xp: number;
  hp: number;
  maxHp: number;
  attack: number;
  defense: number;
  equipped: Array<ItemId>;
  inventory: Array<ItemId>;
};

export type PlayerWithPosition = {
  player: Player;
  position: Position | null;
};

export type RectangularArea = {
  lowerLatitude: number;
  lowerLongitude: number;
  upperLatitude: number;
  upperLongitude: number;
};

export type Area = {
  dimensions: RectangularArea;
  difficulty: number;
};
