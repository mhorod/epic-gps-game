package soturi.model.messages_to_server;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public interface MessageToServer {
    void process(MessageToServerHandler handler);
}
