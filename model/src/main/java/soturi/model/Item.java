package soturi.model;

public record Item(ItemId itemId, String name, ItemType type, long hp, long attack, long defense, String gfxName) {
    public enum ItemType {
        WEAPON, SHIELD, HELMET, ARMOR, BOOTS, GAUNTLETS
    }
}
