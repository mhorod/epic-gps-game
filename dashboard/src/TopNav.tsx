import { PersonCircle } from "react-ionicons";

import "./TopNav.css";
import { Component, useState } from "react";
import { get_json, get_string } from "./backend";

type TopNavProps = { title: string };
type TopNavState = { username: string };
class TopNav extends Component<TopNavProps, TopNavState> {
  constructor(props: TopNavProps) {
    super(props);
    this.state = { username: "" };
    get_string("/v1/current-user").then((res) =>
      this.setState({ username: res }),
    );
  }

  render() {
    return (
      <div className="top-nav">
        <h1> {this.props.title} </h1>
        <div className="user-info">
          <div className="user-name-info">
            <span className="username"> {this.state.username} </span>
            <span className="role"> Admin </span>
          </div>
          <PersonCircle width="40px" height="40px" color="#fff" />
        </div>
      </div>
    );
  }
}

export default TopNav;
