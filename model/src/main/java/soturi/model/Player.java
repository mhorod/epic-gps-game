package soturi.model;

import java.util.List;

public record Player(String name, int lvl, long xp, long hp, Statistics statistics,
                     List<ItemId> equipped, List<ItemId> inventory) { }
