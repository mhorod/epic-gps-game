package model.messages_to_server;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import model.Position;

@Jacksonized
@Builder
public record UpdateLookingPosition(Position position) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.updateLookingPosition(position);
    }
}
