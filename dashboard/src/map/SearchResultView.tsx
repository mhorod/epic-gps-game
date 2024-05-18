import { CSSProperties, MouseEventHandler, ReactNode } from "react";

import IconWrapper from "../IconWrapper";

type SearchResultViewProps = {
  icon: ReactNode;
  name: string;
  type: string;
  onClick: MouseEventHandler<HTMLElement>;
  style: CSSProperties;
};

function SearchResultView(props: SearchResultViewProps) {
  return (
    <div style={props.style}>
      <div className="search-result" onClick={props.onClick}>
        <div className="search-result-name">
          <div className="search-result-icon">
            <IconWrapper icon={props.icon} />
          </div>
          {props.name}
        </div>
        <div className="search-result-type">{props.type}</div>
      </div>
    </div>
  );
}

export default SearchResultView;
