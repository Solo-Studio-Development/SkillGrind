package net.solostudio.skillgrind.enums.keys;

import net.solostudio.skillgrind.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum ItemKeys {
    FILLER_GLASS("grindstone.filler-glass-item"),
    ENCHANTMENT_BOOK("grindstone.enchantment-book-item");

    private final String path;

    ItemKeys(@NotNull final String path) {
        this.path = path;
    }

    public ItemStack getItem() {
        return ItemFactory.createItemFromString(path).orElse(new ItemStack(Material.AIR));
    }

    public void getItem(@NotNull Inventory inventory) {
        ItemFactory.createItemFromString(path, inventory);
    }

    public int getSlot() {
        return ItemFactory.getItemSlotFromString(path);
    }
}
