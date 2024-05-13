import { get_json } from "./backend";
import { Config, EnemyType, EnemyTypeId } from "./model/model";

class ConfigManager {
  config: Config | null = null;
  private enemyTypes: Map<EnemyTypeId, EnemyType> = new Map();
  constructor() {
    get_json("/v1/config").then((cfg) => {
      this.config = cfg;
      for (const enemyType of cfg.enemyTypes)
        this.enemyTypes.set(enemyType.enemyTypeId, enemyType);
    });
  }

  getEnemyTypeById(id: EnemyTypeId): EnemyType | undefined {
    return this.enemyTypes.get(id);
  }
}

const configManager = new ConfigManager();

export default configManager;
