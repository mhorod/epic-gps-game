package soturi.model;

import java.util.List;

public record Player(String name, int lvl, long xp, long hp, long maxHp, long attack, long defense,
                     List<ItemId> equipped, List<ItemId> inventory) { }
