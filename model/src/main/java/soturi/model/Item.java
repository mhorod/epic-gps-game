package soturi.model;

public record Item(ItemId itemId, String name, ItemType type, Statistics statistics, String gfxName) {
    public enum ItemType {
        WEAPON, SHIELD, HELMET, ARMOR, BOOTS, GAUNTLETS
    }
}
