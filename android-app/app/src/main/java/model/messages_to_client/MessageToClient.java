package model.messages_to_client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public interface MessageToClient {
    void process(MessageToClientHandler handler);
}
