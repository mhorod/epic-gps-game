package soturi.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClientHandler;

// TODO nice wrapper around PlayerEntity

@AllArgsConstructor
public class PlayerSession {
    @Getter @NonNull
    private final MessageToClientHandler sender;

    @Getter @Setter @NonNull
    private Position position, looking;
}
