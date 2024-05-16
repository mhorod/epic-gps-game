package soturi.model;

import java.time.Instant;

public record FightRecord(PlayerWithPosition attacker, Enemy defender, FightResult result, Instant time) {
    public FightRecord {
        if (attacker == null || defender == null || time == null)
            throw new RuntimeException();
    }
}
