import { ReactNode } from "react";
import SideNav from "./Sidenav";
import TopNav from "./TopNav";

import "./Main.css";

type MainProps = {
  children?: ReactNode;
};

function Main(props: MainProps) {
  return (
    <div className="main">
      <SideNav />
      <div className="main-right">
        <TopNav />
        {props.children}
      </div>
    </div>
  );
}

export default Main;
