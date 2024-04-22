package model.messages_to_client;

import model.EnemyId;
import model.Result;

public record FightResult(Result result, EnemyId enemyId) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, enemyId);
    }
}
