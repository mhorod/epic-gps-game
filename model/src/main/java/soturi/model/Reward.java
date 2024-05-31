package soturi.model;

import java.util.List;

public record Reward(long xp, List<ItemId> items) {
    public Reward(long xp) {
        this(xp, List.of());
    }
    public Reward() {
        this(0);
    }
}
