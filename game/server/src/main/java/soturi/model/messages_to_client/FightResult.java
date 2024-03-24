package soturi.model.messages_to_client;

import soturi.model.EnemyId;

public record FightResult(Result result, EnemyId enemyId) implements MessageToClient {
    public enum Result {
        WON, LOST
    }

    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, enemyId);
    }
}
