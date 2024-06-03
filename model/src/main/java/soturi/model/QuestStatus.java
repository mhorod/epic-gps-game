package soturi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.With;

@With
public record QuestStatus(String quest, long progress, long goal, Reward reward) {
    @JsonIgnore
    public boolean isFinished() {
        return progress == goal;
    }
}
