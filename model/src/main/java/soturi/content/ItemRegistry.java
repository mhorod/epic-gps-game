package soturi.content;

import soturi.model.Item;
import soturi.model.Item.ItemType;
import soturi.model.ItemId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemRegistry {
    private final Map<ItemId, Item> itemsById = new LinkedHashMap<>();
    private final List<Item> itemList = new ArrayList<>();

    public Item getItemById(ItemId itemId) {
        return itemsById.get(itemId);
    }
    public List<Item> getAllItems() {
        return Collections.unmodifiableList(itemList);
    }

    private void registerItem(long id, String name, ItemType type, long hp, long attack, long defense, String gfxName) {
        String path = "assets/armor/64x64/" + gfxName;
        Item item = new Item(new ItemId(id), name, type, hp, attack, defense, path);

        if (itemsById.put(item.itemId(), item) != null)
            throw new RuntimeException();
        itemList.add(item);
    }

    public ItemRegistry() {
        registerItem(0, "Napierśnik smoka", ItemType.ARMOR, 50, 0, 100, "tile049.png");
        registerItem(1, "Buty smoka", ItemType.BOOTS, 40, 0, 20, "tile034.png");
    }
}
