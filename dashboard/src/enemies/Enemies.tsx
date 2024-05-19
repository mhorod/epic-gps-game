import configManager from "../Config";
import { http_path } from "../backend";
import { EnemyType } from "../model/model";

import "./Enemies.css";

function EnemyTypeView({ type }: { type: EnemyType }) {
  return (
    <div className="enemy-type-view">
      <img src={http_path("/" + type.gfxName)} alt={type.name} />
      <div> {type.name} </div>
    </div>
  );
}

export function Enemies() {
  const enemyTypes = Array.from(configManager.enemyTypes.values());

  return (
    <div className="enemies-wrapper">
      <h1> Enemy Types </h1>
      {enemyTypes.map((t) => (
        <EnemyTypeView type={t} />
      ))}
    </div>
  );
}
