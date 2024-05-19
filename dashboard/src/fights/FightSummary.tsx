import { CaretDown, CaretUp, Skull, Trophy } from "react-ionicons";
import IconWrapper from "../IconWrapper";
import { FightRecord } from "../model/model";
import configManager from "../Config";

type FightSummaryProps = {
  record: FightRecord;
  collapsed: boolean;
  toggleCollapsed: () => void;
};

function FightSummary(props: FightSummaryProps) {
  const icon =
    props.record.result.result === "WON" ? (
      <Trophy color="#A1DD70" />
    ) : (
      <Skull color="#EE4E4E" />
    );

  const toggleIcon = props.collapsed ? <CaretDown /> : <CaretUp />;
  const t = configManager.getEnemyTypeById(props.record.defender.typeId);

  return (
    <div className="fight-record-header">
      <div className="fight-summary">
        <IconWrapper icon={icon} />
        <b>{props.record.attacker.player.name}</b> vs{" "}
        <b>
          {t?.name} lvl {props.record.defender.lvl}
        </b>
      </div>
      <div
        onClick={() => props.toggleCollapsed()}
        style={{ cursor: "pointer" }}
      >
        <IconWrapper icon={toggleIcon} />
      </div>
    </div>
  );
}

export default FightSummary;
