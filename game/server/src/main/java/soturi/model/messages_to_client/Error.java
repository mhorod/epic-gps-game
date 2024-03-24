package soturi.model.messages_to_client;

public record Error(String error) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.error(error);
    }
}
