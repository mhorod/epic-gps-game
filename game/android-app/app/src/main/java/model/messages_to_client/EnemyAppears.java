package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.Enemy;

@Jacksonized
@Builder
public record EnemyAppears(Enemy enemy) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemyAppears(enemy);
    }
}
