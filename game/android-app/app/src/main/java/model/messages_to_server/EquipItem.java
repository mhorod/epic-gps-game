package model.messages_to_server;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.Item;

@Jacksonized
@Builder
public record EquipItem(Item item) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.equipItem(item);
    }
}
