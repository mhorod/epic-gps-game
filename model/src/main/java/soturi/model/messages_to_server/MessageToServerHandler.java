package soturi.model.messages_to_server;

import soturi.model.EnemyId;
import soturi.model.ItemId;
import soturi.model.Position;

public interface MessageToServerHandler {
    void attackEnemy(EnemyId enemyId);
    void disconnect();
    void equipItem(ItemId itemId);
    void unequipItem(ItemId itemId);
    void ping();
    void pong();
    void updateLookingPosition(Position position);
    void updateRealPosition(Position position);
}
