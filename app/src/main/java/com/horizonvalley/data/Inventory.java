package com.horizonvalley.data;

import com.horizonvalley.Config;

public class Inventory {
    private final String[] slots = new String[Config.INVENTORY_SLOTS];
    private final int[] quantities = new int[Config.INVENTORY_SLOTS];

    public void addItem(String id, int qty) {
        // Stack existing
        for (int i = 0; i < slots.length; i++) {
            if (id.equals(slots[i])) {
                quantities[i] += qty;
                return;
            }
        }
        // Empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = id;
                quantities[i] = qty;
                return;
            }
        }
    }

    public String getSlot(int i) {
        if (i < 0 || i >= slots.length) return null;
        return slots[i];
    }

    public int getQuantity(int i) {
        if (i < 0 || i >= quantities.length) return 0;
        return quantities[i];
    }

    public String getHotbarItem(int index) {
        if (index < 0 || index >= Config.HOTBAR_SLOTS) return null;
        return slots[index];
    }

    public void removeHotbar(int index, int amount) {
        if (index < 0 || index >= Config.HOTBAR_SLOTS) return;
        if (slots[index] == null) return;
        quantities[index] -= amount;
        if (quantities[index] <= 0) {
            slots[index] = null;
            quantities[index] = 0;
        }
    }
}
