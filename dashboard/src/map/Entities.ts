import { Enemy, PlayerWithPosition } from "../model/model";

class Entities {
  players: Map<string, PlayerWithPosition> = new Map();
  enemies: Map<number, Enemy> = new Map();

  constructor(old?: Entities) {
    if (old !== undefined) {
      this.players = old.players;
      this.enemies = old.enemies;
    }
  }

  addPlayer(player: PlayerWithPosition) {
    this.players.set(player.player.name, player);
  }
  removePlayer(playerName: string) {
    this.players.delete(playerName);
  }

  addEnemy(enemy: Enemy) {
    this.enemies.set(enemy.enemyId, enemy);
  }

  removeEnemy(enemyId: number) {
    this.enemies.delete(enemyId);
  }
}

export default Entities;
