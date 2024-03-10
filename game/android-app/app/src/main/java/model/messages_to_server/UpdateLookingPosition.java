package model.messages_to_server;

import model.Position;

public record UpdateLookingPosition(Position position) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.updateLookingPosition(position);
    }
}
