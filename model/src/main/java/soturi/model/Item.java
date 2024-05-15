package soturi.model;

public record Item(ItemId itemId, String name, ItemType type, Statistics statistics, String gfxName) {
    public static Item UNKNOWN = new Item(new ItemId(-1), "UNKNOWN", ItemType.WEAPON, new Statistics(0, 0, 0), "");

    public enum ItemType {
        WEAPON, SHIELD, HELMET, ARMOR, BOOTS, GAUNTLETS
    }
}
