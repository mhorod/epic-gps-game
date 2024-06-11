package soturi.model.messages_to_client;

import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.FightRecord;
import soturi.model.FightResult;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.QuestStatus;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class MessageToClientFactory implements MessageToClientHandler {
    protected final Consumer<MessageToClient> consumer;

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
    public void fightDashboardInfo(FightRecord fightRecord) {
        consumer.accept(new FightDashboardInfo(fightRecord));
    }

    @Override
    public void fightInfo(EnemyId enemyId, FightResult fightResult) {
        consumer.accept(new FightInfo(enemyId, fightResult));
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
    public void questUpdate(Instant deadline, List<QuestStatus> quests) {
        consumer.accept(new QuestUpdate(deadline, quests));
    }

    @Override
    public void setConfig(Config config) {
        consumer.accept(new SetConfig(config));
    }
}
