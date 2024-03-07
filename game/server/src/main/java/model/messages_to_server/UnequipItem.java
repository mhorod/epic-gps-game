package model.messages_to_server;

import model.Item;

public record UnequipItem(Item item) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.unequipItem(item);
    }
}
