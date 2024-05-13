package soturi.model.messages_to_client;

import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;

import java.util.List;
import java.util.function.Consumer;

public final class MessageToClientFactory implements MessageToClientHandler {
    private final Consumer<MessageToClient> consumer;

    public MessageToClientFactory(Consumer<MessageToClient> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void disconnect() {
        consumer.accept(new Disconnect());
    }

    @Override
    public void enemiesAppear(List<Enemy> enemies) {
        consumer.accept(new EnemiesAppear(enemies));
    }

    @Override
    public void enemiesDisappear(List<EnemyId> enemyIds) {
        consumer.accept(new EnemiesDisappear(enemyIds));
    }

    @Override
    public void error(String error) {
        consumer.accept(new Error(error));
    }

    @Override
    public void fightResult(Result result, long lostHp, EnemyId enemyId, Loot loot) {
        consumer.accept(new FightResult(result, lostHp, enemyId, loot));
    }

    @Override
    public void meUpdate(Player me) {
        consumer.accept(new MeUpdate(me));
    }

    @Override
    public void ping() {
        consumer.accept(new Ping());
    }

    @Override
    public void playerDisappears(String playerName) {
        consumer.accept(new PlayerDisappears(playerName));
    }

    @Override
    public void playerUpdate(Player player, Position position) {
        consumer.accept(new PlayerUpdate(player, position));
    }

    @Override
    public void pong() {
        consumer.accept(new Pong());
    }

    @Override
    public void setConfig(Config config) {
        consumer.accept(new SetConfig(config));
    }
}
