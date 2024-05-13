import IconWrapper from "../IconWrapper";
import { Enemy, PlayerWithPosition } from "../model/model";

import { Close, Heart, Star, Flash, Shield } from "react-ionicons";

import "./EntityInfo.css";
import configManager from "../Config";

type EntityInfoProps = {
  type: string;
  entity: Enemy | PlayerWithPosition;
  onClose: () => void;
};

function EnemyInfo(props: EntityInfoProps) {
  const enemy = props.entity as Enemy;
  const t = configManager.getEnemyTypeById(enemy.typeId);
  return (
    <div className="entity-info-wrapper">
      <div className="entity-info">
        <div className="card-header">
          <h1> {t?.name} </h1>
          <span className="card-close-icon" onClick={props.onClose}>
            <IconWrapper icon=<Close /> />
          </span>
        </div>
        <div className="card-content">
          <span> Enemy lvl {enemy.lvl} </span>
        </div>
      </div>
    </div>
  );
}

function PlayerInfo(props: EntityInfoProps) {
  const p = props.entity as PlayerWithPosition;
  const player = p.player;
  return (
    <div className="entity-info-wrapper">
      <div className="entity-info">
        <div className="card-header">
          <h1> {player.name} </h1>
          <span className="card-close-icon" onClick={props.onClose}>
            <IconWrapper icon=<Close /> />
          </span>
        </div>
        <div className="card-content">
          <span> Player lvl {player.lvl} </span>
          <ul className="entity-stats-list">
            <li>
              <IconWrapper icon=<Heart /> />{" "}
              <div>
                {" "}
                Health: {player.hp}/{player.statistics.maxHp}{" "}
              </div>
            </li>
            <li>
              <IconWrapper icon=<Star /> /> <div> XP: {player.xp} </div>
            </li>
            <li>
              <IconWrapper icon=<Flash /> />{" "}
              <div> Attack: {player.statistics.attack} </div>
            </li>
            <li>
              <IconWrapper icon=<Shield /> />{" "}
              <div> Defense: {player.statistics.defense} </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

function EntityInfo(props: EntityInfoProps) {
  if (props.type === "Enemy") return EnemyInfo(props);
  else if (props.type === "Player") return PlayerInfo(props);
  else return null;
}

export default EntityInfo;
