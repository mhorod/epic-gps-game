import { Component, useState } from "react";
import { get_json, http_path } from "../backend";
import { FightRecord, Item } from "../model/model";
import configManager from "../Config";
import IconWrapper from "../IconWrapper";
import { CaretDown, CaretUp, Skull, Trophy } from "react-ionicons";

import "./FightHistory.css";
import FightDetails from "./FightDetails";
import FightSummary from "./FightSummary";

function FightRecordView({ record }: { record: FightRecord }) {
  const [collapsed, setCollapsed] = useState(true);
  const className = collapsed ? "collapsed" : "";
  return (
    <div className="fight-record-view">
      <FightSummary
        record={record}
        collapsed={collapsed}
        toggleCollapsed={() => setCollapsed(!collapsed)}
      />
      <div className={"fight-details-wrapper " + className}>
        <FightDetails record={record} collapsed={collapsed} />
      </div>
    </div>
  );
}

type FightHistoryState = {
  fightRecords: FightRecord[];
};

class FightHistory extends Component<{}, FightHistoryState> {
  constructor(props: {}) {
    super(props);
    this.state = { fightRecords: [] };
    get_json("/v1/fight-list?limit=100").then((data) => {
      this.setState({ fightRecords: data });
    });
  }

  render() {
    return (
      <div className="fight-history">
        {this.state.fightRecords.map((r) => {
          return <FightRecordView record={r} />;
        })}
      </div>
    );
  }
}

export default FightHistory;
