package model.messages_to_server;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.EnemyId;

@Jacksonized
@Builder
public record AttackEnemy(EnemyId enemyId) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.attackEnemy(enemyId);
    }
}
