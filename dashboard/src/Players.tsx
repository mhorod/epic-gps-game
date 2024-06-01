import { Component } from "react";
import { PlayerWithPosition } from "./model/model";
import { get_json } from "./backend";
import {
  Heart,
  Shield,
  Star,
  Flash,
  ChevronDownOutline,
  ChevronUpOutline,
} from "react-ionicons";
import IconWrapper from "./IconWrapper";

import "./Players.css";

type PlayerViewProps = {
  player: PlayerWithPosition;
};

type PlayerViewState = {
  collapsed: boolean;
};

class PlayerView extends Component<PlayerViewProps, PlayerViewState> {
  constructor(props: PlayerViewProps) {
    super(props);
    this.state = { collapsed: true };
  }

  toggle() {
    this.setState({ collapsed: !this.state.collapsed });
  }

  render() {
    const p = this.props.player;
    const collapsed = this.state.collapsed ? "collapsed" : "";

    const toggleIcon = collapsed ? (
      <IconWrapper icon=<ChevronDownOutline /> />
    ) : (
      <IconWrapper icon=<ChevronUpOutline /> />
    );

    return (
      <li className="players-list-entry">
        <div className="card-header">
          <div>
            <h1> {p.player.name} </h1>
            <span> Player lvl {p.player.lvl} </span>
          </div>
          <span className="details-toggle" onClick={() => this.toggle()}>
            {toggleIcon}
          </span>
        </div>
        <div className={`player-details ${collapsed}`}>
          <ul className="entity-stats-list">
            <li>
              <IconWrapper icon=<Heart /> />{" "}
              <div>
                {" "}
                Health: {p.player.hp}/{p.player.statistics.maxHp}{" "}
              </div>
            </li>
            <li>
              <IconWrapper icon=<Star /> /> <div> XP: {p.player.xp} </div>
            </li>
            <li>
              <IconWrapper icon=<Flash /> />{" "}
              <div> Attack: {p.player.statistics.attack} </div>
            </li>
            <li>
              <IconWrapper icon=<Shield /> />{" "}
              <div> Defense: {p.player.statistics.defense} </div>
            </li>
          </ul>
        </div>
      </li>
    );
  }
}

type PlayersProps = {};
type PlayersState = {
  players: PlayerWithPosition[];
};

class Players extends Component<PlayersProps, PlayersState> {
  constructor(props: PlayersProps) {
    super(props);
    this.state = { players: [] };

    get_json("/v1/players").then((players) => {
      this.setState({ players });
    });
  }

  render() {
    return (
      <ul className="players-list">
        {this.state.players.map((p) => {
          return <PlayerView player={p} />;
        })}
      </ul>
    );
  }
}

export default Players;
