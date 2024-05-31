package soturi.model.messages_to_client;

import soturi.model.QuestStatus;

import java.time.Instant;
import java.util.List;

public record QuestUpdate(Instant deadline, List<QuestStatus> quests) implements MessageToClient {
    @Override
    public void process(MessageToClientHandler handler) {
        handler.questUpdate(deadline, quests);
    }
}
