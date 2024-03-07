package model.messages_to_server;

import lombok.AllArgsConstructor;
import model.EnemyId;
import model.Item;
import model.Position;

import java.util.function.Consumer;

@AllArgsConstructor
public final class MessageToServerFactory implements MessageToServerHandler {
    private final Consumer<MessageToServer> consumer;

    @Override
    public void attackEnemy(EnemyId enemyId) {
        consumer.accept(new AttackEnemy(enemyId));
    }

    @Override
    public void equipItem(Item item) {
        consumer.accept(new EquipItem(item));
    }

    @Override
    public void loginInfo(String name, String password) {
        consumer.accept(new LoginInfo(name, password));
    }

    @Override
    public void unequipItem(Item item) {
        consumer.accept(new UnequipItem(item));
    }

    @Override
    public void updateLookingPosition(Position position) {
        consumer.accept(new UpdateLookingPosition(position));
    }

    @Override
    public void updateRealPosition(Position position) {
        consumer.accept(new UpdateRealPosition(position));
    }
}
