import configManager from "../Config";
import { rel_path } from "../backend";
import { Enemy } from "../model/model";

function DefenderDetails({ defender }: { defender: Enemy }) {
  const t = configManager.getEnemyTypeById(defender.typeId);
  return (
    <div>
      <b> Defender </b>
      <div className="header-outer">
        <div className="header-name">
          <img src={rel_path("/" + t?.gfxName)} alt={t?.name} />
          <div> {t?.name} </div>
        </div>
        <div className="header-type">Enemy lvl {defender.lvl}</div>
      </div>
    </div>
  );
}

export default DefenderDetails;
