package model;

import java.util.List;

public record FightResult(Result result, long lostHp, long gainXp, List<Item> gainItems) { }
