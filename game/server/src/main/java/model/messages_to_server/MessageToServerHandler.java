package model.messages_to_server;

import model.EnemyId;
import model.Item;
import model.Position;

public interface MessageToServerHandler {
    void attackEnemy(EnemyId enemyId);
    void equipItem(Item item);
    void loginInfo(String name, String password);
    void unequipItem(Item item);
    void updateLookingPosition(Position position);
    void updateRealPosition(Position position);
}
