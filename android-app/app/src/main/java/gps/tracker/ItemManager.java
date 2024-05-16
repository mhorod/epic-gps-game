package gps.tracker;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import soturi.model.Item;
import soturi.model.ItemId;

public class ItemManager {
    private final MainActivity activity;
    private List<ItemId> equippedItemIDs;
    private List<ItemId> inventoryItemIDs;
    private Item.ItemType ItemTypeOfInterest;

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

    private List<Item> itemIDsToItems(@NonNull List<ItemId> itemIDs) {
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

    public List<Item> getItemsOfType(Item.ItemType type) {
        return itemIDsToItems(inventoryItemIDs).stream()
                .filter(item -> item.type().equals(type))
                .collect(Collectors.toList());
    }

    public Item.ItemType getItemTypeOfInterest() {
        return ItemTypeOfInterest;
    }

    public void setItemTypeOfInterest(Item.ItemType type) {
        ItemTypeOfInterest = type;
    }

}
