package soturi.model.messages_to_client;

public record Disconnect() implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.disconnect();
    }
}
