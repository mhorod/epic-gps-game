package soturi.model;

import java.util.List;

public record Loot(long xp, List<ItemId> items) {
    public Loot() {
        this(0, List.of());
    }
}
