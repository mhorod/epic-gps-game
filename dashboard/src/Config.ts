import { get_json } from "./backend";
import { Config, EnemyType, Item } from "./model/model";

class ConfigManager {
  config: Config | null = null;
  readonly enemyTypes: Map<number, EnemyType> = new Map();
  readonly items: Map<number, Item> = new Map();
  constructor() {
    get_json("/v1/config").then((cfg) => {
      this.config = cfg;
      for (const enemyType of cfg.enemyTypes) {
        this.enemyTypes.set(enemyType.typeId, enemyType);
      }

      for (const item of cfg.items) {
        this.items.set(item.itemId, item);
      }
    });
  }

  getEnemyTypeById(id: number): EnemyType | undefined {
    return this.enemyTypes.get(id);
  }

  getItemById(id: number): Item | undefined {
    return this.items.get(id);
  }
}

const configManager = new ConfigManager();

export default configManager;
