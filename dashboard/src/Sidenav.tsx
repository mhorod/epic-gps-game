import {
  Grid,
  Skull,
  People,
  Disc,
  Map,
  Code,
  LogoAndroid,
  StatsChart,
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

function a(to: string, icon: any, text: string) {
  const path = window.location.pathname;
  const className = path === to ? "active" : "";
  return (
    <li className={className}>
      <a href={to}>
        <IconWrapper icon={icon} /> {text}
      </a>
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
        {link("/dashboard/fight-history", <StatsChart />, "Fight History")}
        {a("/swagger-ui/index.html", <Code />, "API")}
        {a("/static/app.apk", <LogoAndroid />, "Android App")}
      </ul>
    </div>
  );
}

export default SideNav;
