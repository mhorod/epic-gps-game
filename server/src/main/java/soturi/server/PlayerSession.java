package soturi.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClientHandler;

public class PlayerSession {
    @Getter @NonNull
    private final MessageToClientHandler sender;

    @Getter @Setter @NonNull
    private Position position, looking;

    public PlayerSession(MessageToClientHandler sender, Position position, Position looking) {
        this.sender = sender;
        this.position = position;
        this.looking = looking;
    }
}
