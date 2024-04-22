package model;

public record Item(String name, ItemType type, long hp, long attack, long defense, int itemId) {
    public enum ItemType {
        WEAPON, SHIELD, HELMET, ARMOR, BOOTS, GAUNTLETS
    }
}
