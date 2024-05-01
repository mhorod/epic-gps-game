package soturi.model.messages_to_server;

import soturi.model.EnemyId;
import soturi.model.Item;
import soturi.model.ItemId;
import soturi.model.Position;

import java.util.function.Consumer;

public final class MessageToServerFactory implements MessageToServerHandler {
    private final Consumer<MessageToServer> consumer;

    public MessageToServerFactory(Consumer<MessageToServer> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void attackEnemy(EnemyId enemyId) {
        consumer.accept(new AttackEnemy(enemyId));
    }

    @Override
    public void disconnect() {
        consumer.accept(new Disconnect());
    }

    @Override
    public void equipItem(ItemId itemId) {
        consumer.accept(new EquipItem(itemId));
    }

    @Override
    public void unequipItem(ItemId itemId) {
        consumer.accept(new UnequipItem(itemId));
    }

    @Override
    public void ping() {
        consumer.accept(new Ping());
    }

    @Override
    public void pong() {
        consumer.accept(new Pong());
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
