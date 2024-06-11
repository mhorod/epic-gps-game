package soturi.server.communication;

import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.QuestStatus;
import soturi.model.messages_to_client.EnemiesAppear;
import soturi.model.messages_to_client.EnemiesDisappear;
import soturi.model.messages_to_client.MeUpdate;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.QuestUpdate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MessageToClientCacheSplitLayer extends MessageToClientFactory {
    public MessageToClientCacheSplitLayer(Consumer<MessageToClient> consumer) {
        super(consumer);
    }

    @Override
    public void enemiesAppear(List<Enemy> enemies) {
        if (enemies.isEmpty())
            return;
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
        if (enemyIds.isEmpty())
            return;
        if (enemyIds.size() < 2000) {
            consumer.accept(new EnemiesDisappear(enemyIds));
            return;
        }
        int mid = enemyIds.size() / 2;
        enemiesDisappear(enemyIds.subList(0, mid));
        enemiesDisappear(enemyIds.subList(mid, enemyIds.size()));
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

    private QuestUpdate lastQuestUpdate;
    @Override
    public void questUpdate(Instant deadline, List<QuestStatus> quests) {
        QuestUpdate newQuestUpdate = new QuestUpdate(deadline, new ArrayList<>(quests));
        if (newQuestUpdate.equals(lastQuestUpdate))
            return;
        lastQuestUpdate = newQuestUpdate;
        consumer.accept(newQuestUpdate);
    }
}
