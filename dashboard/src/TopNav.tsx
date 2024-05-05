import { PersonCircle } from "react-ionicons";

import "./TopNav.css";

function TopNav() {
  return (
    <div className="top-nav">
      <h1> Dashboard </h1>
      <div className="user-info">
        <div className="user-name-info">
          <span className="username"> Gigachad </span>
          <span className="role"> Admin </span>
        </div>
        <PersonCircle width="40px" height="40px" color="#fff" />
      </div>
    </div>
  );
}

export default TopNav;
