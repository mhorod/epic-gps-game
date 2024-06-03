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

public interface MessageToClientHandler {
    void disconnect();
    void enemiesAppear(List<Enemy> enemies);
    void enemiesDisappear(List<EnemyId> enemyIds);
    void error(String error);
    void fightDashboardInfo(FightRecord fightRecord);
    void fightInfo(EnemyId enemyId, FightResult fightResult);
    void meUpdate(Player me);
    void ping();
    void playerDisappears(String playerName);
    void playerUpdate(Player player, Position position);
    void pong();
    void questUpdate(Instant deadline, List<QuestStatus> quests);
    void setConfig(Config config);
}
