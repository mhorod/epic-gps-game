package model;

import java.util.List;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record Player(String name, int lvl, long xp, long hp, long maxHp, long attack, long defense,
                     List<Item> equipped, List<Item> inventory) { }
