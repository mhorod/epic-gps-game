package gps.tracker;

import java.time.Instant;
import java.util.List;

import soturi.model.QuestStatus;

public record CurrentQuests (Instant deadline, List<QuestStatus> quests) {}
