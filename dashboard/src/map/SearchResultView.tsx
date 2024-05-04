import { MouseEventHandler, ReactNode } from "react";

import IconWrapper from "../IconWrapper";

type SearchResultViewProps = {
  icon: ReactNode;
  name: string;
  type: string;
  onClick: MouseEventHandler<HTMLElement>;
};

function SearchResultView(props: SearchResultViewProps) {
  return (
    <div className="search-result" onClick={props.onClick}>
      <div className="search-result-name">
        <div className="search-result-icon">
          <IconWrapper icon={props.icon} />
        </div>
        {props.name}
      </div>
      <div className="search-result-type">{props.type}</div>
    </div>
  );
}

export default SearchResultView;
