package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record PlayerDisappears(String playerName) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.playerDisappears(playerName);
    }
}
