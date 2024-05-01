package soturi.model.messages_to_client;

public record Ping() implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.ping();
    }
}
