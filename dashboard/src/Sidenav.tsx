import {
  Grid,
  Skull,
  People,
  Disc,
  Map,
  Code,
  LogoAndroid,
} from "react-ionicons";
import { Link } from "react-router-dom";
import IconWrapper from "./IconWrapper";

import "./SideNav.css";

function SideNav() {
  return (
    <div className="sidenav">
      <img
        src="/dashboard/img/logo-full.png"
        className="logo"
        alt="Soturi Online"
      />

      <ul>
        <li>
          <Link to="/dashboard">
            <IconWrapper icon=<Grid /> /> Dashboard
          </Link>
        </li>
        <li>
          <Link to="/dashboard/enemies">
            <IconWrapper icon=<Skull /> /> Enemies
          </Link>
        </li>
        <li>
          <Link to="/dashboard/players">
            <IconWrapper icon=<People /> /> Players
          </Link>
        </li>
        <li>
          <Link to="/dashboard/spawn-areas">
            <IconWrapper icon=<Disc /> /> Spawn Areas
          </Link>
        </li>
        <li>
          <Link to="/dashboard/map-view">
            <IconWrapper icon=<Map /> /> Map View
          </Link>
        </li>
        <li>
          <a href="/swagger-ui/index.html">
            <IconWrapper icon=<Code /> /> API
          </a>
        </li>

        <li>
          <a href="/static/app.apk">
            <IconWrapper icon=<LogoAndroid /> /> Android App
          </a>
        </li>
      </ul>
    </div>
  );
}

export default SideNav;
