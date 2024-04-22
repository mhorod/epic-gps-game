package soturi.model.messages_to_server;

import soturi.model.Item;

public record EquipItem(Item item) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.equipItem(item);
    }
}
