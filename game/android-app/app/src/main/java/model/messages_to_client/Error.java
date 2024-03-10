package model.messages_to_client;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record Error(String error) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.error(error);
    }
}
