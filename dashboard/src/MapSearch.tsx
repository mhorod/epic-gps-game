import { Component, MouseEventHandler, ReactNode } from "react";
import { Close, Search, Skull } from "react-ionicons";
import IconWrapper from "./IconWrapper";

import "./MapSearch.css";

type SearchBarProps = {
  onClick: MouseEventHandler<HTMLElement>;
  active: boolean;
};

function SearchBar(props: SearchBarProps) {
  const className = props.active ? "active" : "";
  return (
    <div id="search-bar-wrapper" onClick={props.onClick}>
      <div id="search-bar" className={className}>
        <IconWrapper icon=<Search width="15px" height="15px" /> />
        <input type="text" placeholder="Search map..." />
      </div>
    </div>
  );
}

type SearchResultProps = {
  icon: ReactNode;
  name: string;
  type: string;
};

function SearchResult(props: SearchResultProps) {
  return (
    <div className="search-result">
      <div className="search-result-name">
        <div className="search-result-icon">
          <IconWrapper icon={props.icon} />
        </div>
        {props.name}
      </div>
      <div className="search-result-type">{props.type}</div>
    </div>
  );
}

type SearchModalProps = {
  active: boolean;
};

function SearchModal(props: SearchModalProps) {
  const className = props.active ? "active" : "";
  return (
    <div id="search-modal-wrapper">
      <div id="search-modal" className={className}>
        <div className="card-header">
          <h1> Search on the map </h1>
          <span id="search-modal-close-icon" className="card-close-icon">
            <Close />
          </span>
        </div>
        <div id="search-bar-space"></div>
        <div id="search-results">
          <SearchResult
            icon=<Skull width="19px" height="18px" />
            name="Mrówka"
            type="Enemy lvl 1"
          />
        </div>
      </div>
    </div>
  );
}

type MapSearchProps = {};
type MapSearchState = {
  active: boolean;
};

class MapSearch extends Component<MapSearchProps, MapSearchState> {
  constructor(props: MapSearchProps) {
    super(props);
    this.state = { active: false };
  }

  render() {
    return (
      <div>
        <SearchBar
          onClick={() => this.beginSearch()}
          active={this.state.active}
        />
        <SearchModal active={this.state.active} />
      </div>
    );
  }

  beginSearch() {
    this.setState({ active: !this.state.active });
  }
}

export default MapSearch;
