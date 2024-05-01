package soturi.model.messages_to_client;

public record Pong() implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.pong();
    }
}
