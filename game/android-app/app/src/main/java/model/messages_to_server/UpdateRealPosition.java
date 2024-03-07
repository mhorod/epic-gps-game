package model.messages_to_server;

import model.Position;

public record UpdateRealPosition(Position position) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.updateRealPosition(position);
    }
}
