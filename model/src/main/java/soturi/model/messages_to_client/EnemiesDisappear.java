package soturi.model.messages_to_client;

import soturi.model.EnemyId;

import java.util.List;

public record EnemiesDisappear(List<EnemyId> enemyIds) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemiesDisappear(enemyIds);
    }
}
