package soturi.model.messages_to_client;

import soturi.model.FightRecord;

public record FightDashboardInfo(FightRecord fightRecord) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightDashboardInfo(fightRecord);
    }
}
