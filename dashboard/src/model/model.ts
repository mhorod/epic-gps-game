export type Position = {
  latitude: number;
  longitude: number;
};

export type Enemy = {
  typeId: number;
  enemyId: number;
  lvl: number;
  position: Position;
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
  equipped: Array<number>;
  inventory: Array<number>;
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

export type EnemyType = {
  typeId: number;
  name: string;
  gfxName: string;
};

export type Config = {
  enemyTypes: EnemyType[];
  items: Item[];
};

export type Loot = {
  xp: number;
  items: number[];
};

export type FightResult = {
  result: string;
  lostHp: number;
  loot: Loot;
};

export type FightRecord = {
  attacker: PlayerWithPosition;
  defender: Enemy;
  result: FightResult;
  time: string;
};

export type ItemType =
  | "WEAPON"
  | "SHIELD"
  | "HELMET"
  | "ARMOR"
  | "BOOTS"
  | "GAUNTLETS";

export type Item = {
  itemId: number;
  name: string;
  type: ItemType;
  statistics: Statistics;
  gfxName: string;
};
