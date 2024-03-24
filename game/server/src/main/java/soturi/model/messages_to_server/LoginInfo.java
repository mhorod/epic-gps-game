package soturi.model.messages_to_server;

public record LoginInfo(String name, String password) implements MessageToServer {
    @Override
    public void process(MessageToServerHandler handler) {
        handler.loginInfo(name, password);
    }
}
