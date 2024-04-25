package soturi.model.messages_to_server;

import soturi.model.EnemyId;
import soturi.model.Item;
import soturi.model.Position;

public interface MessageToServerHandler {
    void attackEnemy(EnemyId enemyId);
    void disconnect();
    void equipItem(Item item);
    void unequipItem(Item item);
    void ping();
    void pong();
    void updateLookingPosition(Position position);
    void updateRealPosition(Position position);
}
