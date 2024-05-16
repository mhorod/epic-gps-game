package gps.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import soturi.model.Item;
import soturi.model.ItemId;

public class ItemManager {
    private final MainActivity activity;
    private List<ItemId> equippedItemIDs;
    private List<ItemId> inventoryItemIDs;

    public ItemManager(MainActivity activity) {
        equippedItemIDs = new ArrayList<>();
        inventoryItemIDs = new ArrayList<>();
        this.activity = activity;
    }

    public List<Item> getEquippedItems() {
        return itemIDsToItems(equippedItemIDs);
    }

    public List<Item> getItems() {
        return itemIDsToItems(inventoryItemIDs);
    }

    private List<Item> itemIDsToItems(List<ItemId> itemIDs) {
        return itemIDs.stream()
                .map(activity.getGameRegistry()::getItemById).collect(Collectors.toList());
    }

    public void setEquippedItemIDs(List<ItemId> equippedItems) {
        this.equippedItemIDs = equippedItems;
    }


    public void setInventoryItemIDs(List<ItemId> inventoryItems) {
        this.inventoryItemIDs = inventoryItems;
    }

    public boolean thereExistsItemWithType(Item.ItemType type) {
        return itemIDsToItems(inventoryItemIDs).stream()
                .anyMatch(item -> item.type().equals(type));
    }

}
