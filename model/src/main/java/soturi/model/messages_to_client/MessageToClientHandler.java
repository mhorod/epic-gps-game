package soturi.model.messages_to_client;

import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;

import java.util.List;

public interface MessageToClientHandler {
    void disconnect();
    void enemiesAppear(List<Enemy> enemies);
    void enemiesDisappear(List<EnemyId> enemyIds);
    void error(String error);
    void fightResult(Result result, long lostHp, EnemyId enemyId, Loot loot);
    void meUpdate(Player me);
    void ping();
    void playerDisappears(String playerName);
    void playerUpdate(Player player, Position position);
    void pong();
    void setConfig(Config config);
}
