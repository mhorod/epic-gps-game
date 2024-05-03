import { Grid, Skull, People, Disc, Map, Code } from "react-ionicons";
import { Link } from "react-router-dom";
import IconWrapper from "./IconWrapper";

import "./SideNav.css";

function SideNav() {
  return (
    <div className="sidenav">
      <img src="img/logo-full.png" className="logo" alt="Soturi Online" />

      <ul>
        <li>
          <Link to="/">
            <IconWrapper icon=<Grid /> /> Dashboard
          </Link>
        </li>
        <li>
          <Link to="/enemies">
            <IconWrapper icon=<Skull /> /> Enemies
          </Link>
        </li>
        <li>
          <Link to="/players">
            <IconWrapper icon=<People /> /> Players
          </Link>
        </li>
        <li>
          <Link to="/spawn-areas">
            <IconWrapper icon=<Disc /> /> Spawn Areas
          </Link>
        </li>
        <li>
          <Link to="/map-view">
            <IconWrapper icon=<Map /> /> Map View
          </Link>
        </li>
        <li>
          <a href="/swagger-ui/index.html">
            <IconWrapper icon=<Code /> /> API
          </a>
        </li>
      </ul>
    </div>
  );
}

export default SideNav;
