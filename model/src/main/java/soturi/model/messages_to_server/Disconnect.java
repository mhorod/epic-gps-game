package soturi.model.messages_to_server;

public record Disconnect() implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.disconnect();
    }
}
