package soturi.model.messages_to_server;

import soturi.model.Item;

public record UnequipItem(Item item) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.unequipItem(item);
    }
}
