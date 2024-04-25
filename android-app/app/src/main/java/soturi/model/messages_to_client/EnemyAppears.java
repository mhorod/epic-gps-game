package soturi.model.messages_to_client;

import soturi.model.Enemy;

public record EnemyAppears(Enemy enemy) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemyAppears(enemy);
    }
}
