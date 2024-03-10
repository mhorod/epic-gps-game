package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.EnemyId;

@Jacksonized
@Builder
public record EnemyDisappears(EnemyId enemyId) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemyDisappears(enemyId);
    }
}
