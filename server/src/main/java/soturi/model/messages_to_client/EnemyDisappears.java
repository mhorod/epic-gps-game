package soturi.model.messages_to_client;

import soturi.model.EnemyId;

public record EnemyDisappears(EnemyId enemyId) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemyDisappears(enemyId);
    }
}
