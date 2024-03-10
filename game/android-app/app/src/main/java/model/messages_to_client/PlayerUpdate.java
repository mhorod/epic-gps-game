package model.messages_to_client;

import model.Player;
import model.Position;

public record PlayerUpdate(Player player, Position position) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.playerUpdate(player, position);
    }
}
