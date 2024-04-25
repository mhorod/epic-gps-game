package soturi.model.messages_to_server;

public record Pong() implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.pong();
    }
}
