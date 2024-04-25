package soturi.model.messages_to_server;

import lombok.AllArgsConstructor;
import soturi.model.EnemyId;
import soturi.model.Item;
import soturi.model.Position;

import java.util.function.Consumer;

@AllArgsConstructor
public final class MessageToServerFactory implements MessageToServerHandler {
    private final Consumer<MessageToServer> consumer;

    @Override
    public void attackEnemy(EnemyId enemyId) {
        consumer.accept(new AttackEnemy(enemyId));
    }

    @Override
    public void disconnect() {
        consumer.accept(new Disconnect());
    }

    @Override
    public void equipItem(Item item) {
        consumer.accept(new EquipItem(item));
    }

    @Override
    public void unequipItem(Item item) {
        consumer.accept(new UnequipItem(item));
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
