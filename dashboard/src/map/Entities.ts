import { Enemy, EnemyId, PlayerWithPosition } from "../model/model";

class Entities {
  players: Map<string, PlayerWithPosition> = new Map();
  enemies: Map<string, Enemy> = new Map();

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
    this.enemies.set(enemy.enemyId.id, enemy);
  }

  removeEnemy(enemyId: EnemyId) {
    this.enemies.delete(enemyId.id);
  }
}

export default Entities;
