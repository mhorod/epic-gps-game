import { Component, MouseEventHandler } from "react";
import { Search } from "react-ionicons";

import IconWrapper from "../IconWrapper";
import SearchSettings from "./SearchSettings";

type SearchBarProps = {
  onClick: MouseEventHandler<HTMLElement>;
  active: boolean;
  updateSearch: (s: SearchSettings) => void;
};

type SearchBarState = {
  searchValue: string;
};

class SearchBar extends Component<SearchBarProps, SearchBarState> {
  constructor(props: SearchBarProps) {
    super(props);
    this.state = { searchValue: "" };
  }

  render() {
    const className = this.props.active ? "active" : "";
    return (
      <div id="search-bar-wrapper" onClick={this.props.onClick}>
        <div id="search-bar" className={className}>
          <IconWrapper icon=<Search width="15px" height="15px" /> />
          <input
            type="text"
            placeholder="Search map..."
            onChange={(e) => this.updateSearchValue(e.target.value)}
          />
        </div>
      </div>
    );
  }

  updateSearchValue(value: string) {
    this.setState({ searchValue: value });
    this.props.updateSearch({ searchValue: value });
  }
}

export default SearchBar;
