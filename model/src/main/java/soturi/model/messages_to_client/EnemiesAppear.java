package soturi.model.messages_to_client;

import soturi.model.Enemy;

import java.util.List;

public record EnemiesAppear(List<Enemy> enemies) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.enemiesAppear(enemies);
    }
}
