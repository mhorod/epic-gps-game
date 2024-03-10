package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.Player;
import model.Position;

@Jacksonized
@Builder
public record PlayerUpdate(Player player, Position position) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.playerUpdate(player, position);
    }
}
