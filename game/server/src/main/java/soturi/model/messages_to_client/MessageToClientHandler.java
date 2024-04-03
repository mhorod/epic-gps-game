package soturi.model.messages_to_client;

import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;

public interface MessageToClientHandler {
    void enemyAppears(Enemy enemy);
    void enemyDisappears(EnemyId enemyId);
    void error(String error);
    void fightResult(Result result, EnemyId enemyId);
    void meUpdate(Player me);
    void playerDisappears(String playerName);
    void playerUpdate(Player player, Position position);
}
