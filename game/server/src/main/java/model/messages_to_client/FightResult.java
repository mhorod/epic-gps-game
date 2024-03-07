package model.messages_to_client;

import model.EnemyId;

public record FightResult(Result result, EnemyId enemyId) implements MessageToClient {
    enum Result {
        WON, LOST
    }

    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, enemyId);
    }
}
