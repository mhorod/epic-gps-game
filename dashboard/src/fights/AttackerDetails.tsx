import { Heart, Star, Flash, Shield, PersonCircle } from "react-ionicons";
import configManager from "../Config";
import IconWrapper from "../IconWrapper";
import { rel_path } from "../backend";
import { Item, PlayerWithPosition } from "../model/model";

function equippedItems(items: Item[]) {
  if (items.length === 0) return <div> </div>;
  return (
    <div>
      Equipped
      <div className="attacker-equipped-items">
        {items.map((item) => {
          return (
            <div className="item-short">
              <img
                className="item-image"
                src={rel_path("/" + item.gfxName)}
                alt=""
              />
              {item.name}
            </div>
          );
        })}
      </div>
    </div>
  );
}

function attackerStats(attacker: PlayerWithPosition) {
  return (
    <div className="attacker-stats">
      <div className="attacker-stats-list">
        <div>
          <IconWrapper icon=<Heart /> />
          <div>
            Health: {attacker.player.hp}/{attacker.player.statistics.maxHp}{" "}
          </div>
        </div>
        <div>
          <IconWrapper icon=<Star /> />
          <div> XP: {attacker.player.xp} </div>
        </div>
        <div>
          <IconWrapper icon=<Flash /> />
          <div> Attack: {attacker.player.statistics.attack} </div>
        </div>
        <div>
          <IconWrapper icon=<Shield /> />
          <div> Defense: {attacker.player.statistics.defense} </div>
        </div>
      </div>
    </div>
  );
}

function AttackerDetails({ attacker }: { attacker: PlayerWithPosition }) {
  const items: Item[] = attacker.player.equipped.map(
    (id) => configManager.getItemById(id)!,
  );

  return (
    <div>
      <b> Attacker </b>
      <div className="attacker-details">
        <div className="header-outer">
          <div className="header-name">
            <IconWrapper icon={<PersonCircle width="32px" height="32px" />} />
            <div> {attacker.player.name} </div>
          </div>
          <div className="header-type">Player lvl {attacker.player.lvl}</div>
        </div>
        {attackerStats(attacker)}
        {equippedItems(items)}
      </div>
    </div>
  );
}

export default AttackerDetails;
