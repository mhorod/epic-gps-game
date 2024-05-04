import { Enemy, PlayerWithPosition } from "../model/model";

class SearchResult {
  constructor(
    readonly kind: string,
    readonly value: PlayerWithPosition | Enemy,
  ) {}
  static ofPlayer(player: PlayerWithPosition) {
    return new SearchResult("Player", player);
  }
  static ofEnemy(enemy: Enemy) {
    return new SearchResult("Enemy", enemy);
  }
}

export default SearchResult;
