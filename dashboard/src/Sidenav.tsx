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

function link(to: string, icon: any, text: string) {
  const path = window.location.pathname;
  const className = path === to ? "active" : "";
  return (
    <li className={className}>
      <Link to={to}>
        <IconWrapper icon={icon} /> {text}
      </Link>
    </li>
  );
}

function SideNav() {
  return (
    <div className="sidenav">
      <img
        src="/dashboard/img/logo-full.png"
        className="logo"
        alt="Soturi Online"
      />
      <ul>
        {link("/dashboard", <Grid />, "Dashboard")}
        {link("/dashboard/enemies", <Skull />, "Enemies")}
        {link("/dashboard/players", <People />, "Players")}
        {link("/dashboard/spawn-areas", <Disc />, "Spawn Areas")}
        {link("/dashboard/map-view", <Map />, "Map View")}
        {link("/swagger-ui/index.html", <Code />, "API")}
        {link("/static/app.apk", <LogoAndroid />, "Android App")}
      </ul>
    </div>
  );
}

export default SideNav;
