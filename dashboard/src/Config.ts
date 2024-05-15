import { get_json } from "./backend";
import { Config, EnemyType } from "./model/model";

class ConfigManager {
  config: Config | null = null;
  private enemyTypes: Map<number, EnemyType> = new Map();
  constructor() {
    get_json("/v1/config").then((cfg) => {
      this.config = cfg;
      for (const enemyType of cfg.enemyTypes) {
        this.enemyTypes.set(enemyType.typeId, enemyType);
      }
    });
  }

  getEnemyTypeById(id: number): EnemyType | undefined {
    console.log(this.enemyTypes);
    console.log(id, this.enemyTypes.get(id));
    return this.enemyTypes.get(id);
  }
}

const configManager = new ConfigManager();

export default configManager;
