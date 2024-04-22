package soturi.model.messages_to_server;

import soturi.model.EnemyId;

public record AttackEnemy(EnemyId enemyId) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.attackEnemy(enemyId);
    }
}
