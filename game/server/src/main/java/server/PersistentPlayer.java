package server;

import model.Item;
import model.Player;

import java.util.List;

public record PersistentPlayer(String name, long xp, long hp, List<Item> equipped, List<Item> inventory) {
    public PersistentPlayer(String name) {
        this(name, 0, 1, List.of(), List.of());
    }

    public Player toPlayer() {
        long lvl = xp / 5 + 1;
        long maxHp = lvl * 5;
        long attack = lvl * 3;
        long defense = lvl * 2;
        return new Player(name, (int) lvl, xp, hp, maxHp, attack, defense, equipped, inventory);
    }

    public PersistentPlayer gainXp(long gainXp) {
        return new PersistentPlayer(name, xp + gainXp, hp, equipped, inventory);
    }
}
