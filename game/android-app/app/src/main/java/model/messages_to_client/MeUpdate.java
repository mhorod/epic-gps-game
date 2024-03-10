package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.Player;

// TODO investigate why these annotations are needed on android and not on boot spring
// probably something to do with jackson autoconfiguration, but idk
@Jacksonized
@Builder
public record MeUpdate(Player me) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.meUpdate(me);
    }
}
