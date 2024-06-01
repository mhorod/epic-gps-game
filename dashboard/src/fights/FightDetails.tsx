import { FightRecord } from "../model/model";
import AttackerDetails from "./AttackerDetails";
import DefenderDetails from "./DefenderDetails";
import ResultDetails from "./ResultDetails";

type FightDetailsProps = {
  record: FightRecord;
  collapsed: boolean;
};

function FightDetails(props: FightDetailsProps) {
  const detailsClass = props.collapsed ? "collapsed" : "";
  const date = new Date(props.record.time);

  return (
    <div className={"fight-record-details " + detailsClass}>
      <div>
        Fight date: {date.toLocaleString("pl-PL", { timeZone: "EST" })}{" "}
      </div>
      <AttackerDetails attacker={props.record.attacker} />
      <hr />
      <DefenderDetails defender={props.record.defender} />
      <hr />
      <ResultDetails result={props.record.result} />
    </div>
  );
}

export default FightDetails;
