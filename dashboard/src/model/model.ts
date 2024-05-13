export type Position = {
  latitude: number;
  longitude: number;
};

export type EnemyId = {
  id: string;
};

export type Enemy = {
  enemyTypeId: EnemyTypeId;
  enemyId: EnemyId;
  lvl: number;
  position: Position;
};

export type ItemId = {
  id: string;
};

export type Statistics = {
  maxHp: number;
  attack: number;
  defense: number;
};

export type Player = {
  name: string;
  lvl: number;
  xp: number;
  hp: number;
  statistics: Statistics;
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

export type Polygon = {
  points: Position[];
};

export type PolygonWithDifficulty = {
  polygon: Polygon;
  difficulty: number;
};

export type EnemyTypeId = { id: number };

export type EnemyType = {
  enemyTypeId: EnemyTypeId;
  name: string;
  gfxName: string;
};

export type Config = {
  enemyTypes: EnemyType[];
};
