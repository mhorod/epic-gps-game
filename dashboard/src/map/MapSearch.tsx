import { Component, createRef, RefObject } from "react";

import SearchBar from "./SearchBar";
import SearchModal from "./SearchModal";
import SearchResult from "./SearchResult";
import SearchSettings from "./SearchSettings";

import { Position } from "../model/model";
import "./MapSearch.css";
import Entities from "./Entities";
import configManager from "../Config";

type MapSearchProps = {
  search: (settings: SearchSettings) => SearchResult[];
  zoomOn: (p: Position) => void;
  active: boolean;
  openSearch: () => void;
  closeSearch: () => void;
};

type MapSearchState = {
  searchResults: SearchResult[];
};

class MapSearch extends Component<MapSearchProps, MapSearchState> {
  elementRef: RefObject<HTMLDivElement>;
  searchSettings: SearchSettings;

  constructor(props: MapSearchProps) {
    super(props);
    this.state = {
      searchResults: [],
    };
    this.searchSettings = { searchValue: "" };
    this.elementRef = createRef();
  }

  componentDidMount(): void {
    this.handleOutsideClicks();
  }

  handleOutsideClicks() {
    document.addEventListener("click", (e) => {
      if (e.target instanceof Node) {
        if (!this.elementRef.current?.contains(e.target)) {
          this.props.closeSearch();
        }
      }
    });
  }

  render() {
    return (
      <div ref={this.elementRef}>
        <SearchBar
          onClick={this.props.openSearch}
          active={this.props.active}
          updateSearch={(settings) => this.updateSearch(settings)}
        />
        <SearchModal
          active={this.props.active}
          closeSearch={this.props.closeSearch}
          searchResults={this.state.searchResults}
          zoomOn={this.props.zoomOn}
        />
      </div>
    );
  }

  updateSearch(settings: SearchSettings) {
    this.searchSettings = settings;
    this.refreshSearchResults();
  }

  refreshSearchResults() {
    if (this.searchSettings.searchValue === "") {
      this.setState({
        ...this.state,
        searchResults: [],
      });
      return;
    }

    this.setState({
      ...this.state,
      searchResults: this.props.search(this.searchSettings),
    });
  }
}

export default MapSearch;
