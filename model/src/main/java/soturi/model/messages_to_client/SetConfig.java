package soturi.model.messages_to_client;

import soturi.model.Config;

public record SetConfig(Config config) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.setConfig(config);
    }
}
