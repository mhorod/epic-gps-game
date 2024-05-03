import { ReactNode } from "react";
import SideNav from "./Sidenav";
import TopNav from "./TopNav";

import "./Main.css";

function Main({ children }: { children: ReactNode }) {
  return (
    <div className="main">
      <SideNav />
      <div className="main-right">
        <TopNav />
        {children}
      </div>
    </div>
  );
}

export default Main;
