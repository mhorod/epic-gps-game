package soturi.model.messages_to_client;

import soturi.model.EnemyId;
import soturi.model.Result;

public record FightResult(Result result, EnemyId enemyId) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, enemyId);
    }
}
