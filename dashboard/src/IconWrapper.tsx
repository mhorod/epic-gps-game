import { ReactNode } from "react";
import "./IconWrapper.css";

/**
 * Wraps IonIcons into a square element because for some reason
 * they have additional 4.5px of height
 */
function IconWrapper({ icon }: { icon: ReactNode }) {
  return <div className="icon-wrapper"> {icon} </div>;
}

export default IconWrapper;
