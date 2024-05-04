package soturi.model.messages_to_client;

import soturi.model.EnemyId;
import soturi.model.Loot;
import soturi.model.Result;

public record FightResult(Result result, long lostHp, EnemyId enemyId, Loot loot) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, lostHp, enemyId, loot);
    }
}
