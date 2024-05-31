package soturi.model;

import lombok.With;

@With
public record QuestStatus(String quest, long progress, long goal, Reward reward) { }
