package soturi.model.messages_to_client;

import lombok.AllArgsConstructor;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;

import java.util.function.Consumer;

@AllArgsConstructor
public final class MessageToClientFactory implements MessageToClientHandler {
    private final Consumer<MessageToClient> consumer;

    @Override
    public void enemyAppears(Enemy enemy) {
        consumer.accept(new EnemyAppears(enemy));
    }

    @Override
    public void enemyDisappears(EnemyId enemyId) {
        consumer.accept(new EnemyDisappears(enemyId));
    }

    @Override
    public void error(String error) {
        consumer.accept(new Error(error));
    }

    @Override
    public void fightResult(Result result, EnemyId enemyId) {
        consumer.accept(new FightResult(result, enemyId));
    }

    @Override
    public void meUpdate(Player me) {
        consumer.accept(new MeUpdate(me));
    }

    @Override
    public void playerDisappears(String playerName) {
        consumer.accept(new PlayerDisappears(playerName));
    }

    @Override
    public void playerUpdate(Player player, Position position) {
        consumer.accept(new PlayerUpdate(player, position));
    }
}
