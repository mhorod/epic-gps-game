package soturi.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClientHandler;

@RequiredArgsConstructor
public final class PlayerSession {
    @Getter @NonNull
    private final MessageToClientHandler sender;

    @Getter @Setter
    private Position position, looking;
}
