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
        if (enemies.size() < 2000) {
            consumer.accept(new EnemiesAppear(enemies));
            return;
        }
        int mid = enemies.size() / 2;
        enemiesAppear(enemies.subList(0, mid));
        enemiesAppear(enemies.subList(mid, enemies.size()));
    }

    @Override
    public void enemiesDisappear(List<EnemyId> enemyIds) {
        if (enemyIds.size() < 2000) {
            consumer.accept(new EnemiesDisappear(enemyIds));
            return;
        }
        int mid = enemyIds.size() / 2;
        enemiesDisappear(enemyIds.subList(0, mid));
        enemiesDisappear(enemyIds.subList(mid, enemyIds.size()));
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

    private MeUpdate lastMeUpdate;
    @Override
    public void meUpdate(Player me) {
        MeUpdate newMeUpdate = new MeUpdate(me);
        if (newMeUpdate.equals(lastMeUpdate))
            return;
        lastMeUpdate = newMeUpdate;
        consumer.accept(newMeUpdate);
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

    private QuestUpdate lastQuestUpdate;
    @Override
    public void questUpdate(Instant deadline, List<QuestStatus> quests) {
        QuestUpdate newQuestUpdate = new QuestUpdate(deadline, quests);
        if (newQuestUpdate.equals(lastQuestUpdate))
            return;
        lastQuestUpdate = newQuestUpdate;
        consumer.accept(newQuestUpdate);
    }

    @Override
    public void setConfig(Config config) {
        consumer.accept(new SetConfig(config));
    }
}
