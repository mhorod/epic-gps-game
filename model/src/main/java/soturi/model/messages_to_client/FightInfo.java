package soturi.model.messages_to_client;

import soturi.model.EnemyId;
import soturi.model.FightResult;

public record FightInfo(EnemyId enemyId, FightResult result) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightInfo(enemyId, result);
    }
}
