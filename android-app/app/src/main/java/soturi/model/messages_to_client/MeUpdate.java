package soturi.model.messages_to_client;

import soturi.model.Player;

public record MeUpdate(Player me) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.meUpdate(me);
    }
}
