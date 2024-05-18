import { CSSProperties, MouseEventHandler } from "react";
import { Skull, Close, Person } from "react-ionicons";

import { Enemy, PlayerWithPosition, Position } from "../model/model";

import { FixedSizeList as List, ListChildComponentProps } from "react-window";

import IconWrapper from "../IconWrapper";
import SearchResult from "./SearchResult";
import SearchResultView from "./SearchResultView";

import configManager from "../Config";

type SearchModalProps = {
  active: boolean;
  closeSearch: MouseEventHandler<HTMLElement>;
  searchResults: SearchResult[];
  zoomOn: (p: Position) => void;
};

function resultView(
  r: SearchResult,
  zoomOn: (p: Position) => void,
  style: CSSProperties,
) {
  if (r.kind === "Enemy") {
    const enemy = r.value as Enemy;
    const t = configManager.getEnemyTypeById(enemy.typeId);
    return (
      <SearchResultView
        key={"enemy-" + enemy.enemyId}
        icon=<Skull />
        name={t?.name || "undefined"}
        type={"Enemy lvl " + enemy.lvl}
        onClick={() => zoomOn(enemy.position)}
        style={style}
      />
    );
  } else if (r.kind === "Player") {
    const player = r.value as PlayerWithPosition;
    return (
      <SearchResultView
        key={"player-" + player.player.name}
        icon=<Person />
        name={player.player.name}
        type={"Player lvl " + player.player.lvl}
        onClick={() => {
          if (player.position !== null) zoomOn(player.position);
        }}
        style={style}
      />
    );
  } else {
    console.log("???");
    return <div> </div>;
  }
}

function SearchModal(props: SearchModalProps) {
  const className = props.active ? "active" : "";
  const Element: React.FC<ListChildComponentProps> = ({
    index,
    style,
  }: {
    index: number;
    style: CSSProperties;
  }) => {
    return resultView(props.searchResults[index], props.zoomOn, style);
  };
  return (
    <div id="search-modal-wrapper">
      <div id="search-modal" className={className}>
        <div className="card-header">
          <h1> Search on the map </h1>
          <span
            id="search-modal-close-icon"
            className="card-close-icon"
            onClick={props.closeSearch}
          >
            <IconWrapper icon=<Close /> />
          </span>
        </div>
        <div id="search-bar-space"></div>
        <List
          width={460}
          height={400}
          itemCount={props.searchResults.length}
          itemSize={50}
        >
          {Element}
        </List>
      </div>
    </div>
  );
}

export default SearchModal;
