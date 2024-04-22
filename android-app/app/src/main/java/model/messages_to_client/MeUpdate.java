package model.messages_to_client;

import model.Player;

public record MeUpdate(Player me) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.meUpdate(me);
    }
}
