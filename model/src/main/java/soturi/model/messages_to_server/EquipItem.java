package soturi.model.messages_to_server;

import soturi.model.ItemId;

public record EquipItem(ItemId itemId) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.equipItem(itemId);
    }
}
