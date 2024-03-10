package model;

import java.util.List;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record FightResult(Result result, long lostHp, long gainXp, List<Item> gainItems) { }
