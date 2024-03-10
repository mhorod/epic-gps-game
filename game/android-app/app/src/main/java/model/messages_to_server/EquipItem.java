package model.messages_to_server;

import model.Item;

public record EquipItem(Item item) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.equipItem(item);
    }
}
