package model.messages_to_client;

import model.Enemy;
import model.EnemyId;
import model.Player;
import model.Position;
import model.Result;

public interface MessageToClientHandler {
    void enemyAppears(Enemy enemy);
    void enemyDisappears(EnemyId enemyId);
    void error(String error);
    void fightResult(Result result, EnemyId enemyId);
    void meUpdate(Player me);
    void playerDisappears(String playerName);
    void playerUpdate(Player player, Position position);
}
