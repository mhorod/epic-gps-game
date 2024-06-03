import { HeartHalf, Skull, Star, Trophy } from "react-ionicons";
import configManager from "../Config";
import IconWrapper from "../IconWrapper";
import { rel_path } from "../backend";
import { FightResult, Item } from "../model/model";

function lootView(loot: Item[]) {
  if (loot.length === 0) return <div></div>;
  return (
    <div>
      Loot
      <div>
        {loot.map((item) => {
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

function ResultDetails({ result }: { result: FightResult }) {
  const loot: Item[] = result.reward.items.map(
    (id) => configManager.getItemById(id)!,
  );

  const color = result.result === "WON" ? "#A1DD70" : "#EE4E4E";

  const icon =
    result.result === "WON" ? (
      <Trophy color={color} />
    ) : (
      <Skull color={color} />
    );

  return (
    <div>
      <b>Result</b>
      <div className="result-details">
        <div style={{ color: color }} className="fight-result-summary">
          <IconWrapper icon={icon} /> <div> {result.result} </div>
        </div>
        <div>
          <div className="result-stats-entry">
            <IconWrapper icon={<HeartHalf />} /> Lost HP: {result.lostHp}{" "}
          </div>
          <div className="result-stats-entry">
            <IconWrapper icon={<Star />} /> Gained XP: {result.reward.xp}{" "}
          </div>
        </div>
        {lootView(loot)}
      </div>
    </div>
  );
}

export default ResultDetails;
