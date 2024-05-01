package soturi.model.messages_to_server;

public record Ping() implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.ping();
    }
}
