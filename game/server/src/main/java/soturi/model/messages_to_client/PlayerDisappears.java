package soturi.model.messages_to_client;

public record PlayerDisappears(String playerName) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.playerDisappears(playerName);
    }
}
