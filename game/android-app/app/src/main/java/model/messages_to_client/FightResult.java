package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.EnemyId;
import model.Result;

@Jacksonized
@Builder
public record FightResult(Result result, EnemyId enemyId) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.fightResult(result, enemyId);
    }
}
