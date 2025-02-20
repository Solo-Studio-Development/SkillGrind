package net.solostudio.skillgrind.gui;

import net.solostudio.skillgrind.data.MenuData;
import net.solostudio.skillgrind.processor.MessageProcessor;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public abstract class Menu implements InventoryHolder {
    protected MenuData menuData;
    protected Inventory inventory;

    private boolean closing = false;

    public Menu(@NotNull MenuData menuData) {
        this.menuData = menuData;
    }

    public abstract void handleMenu(final InventoryClickEvent event);
    public void onClose(final InventoryCloseEvent event) {}
    public abstract void setMenuItems();

    public abstract String getMenuName();

    public abstract int getSlots();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open() {
        closing = false;
        inventory = Bukkit.createInventory(this, getSlots(), MessageProcessor.process(getMenuName()));
        setMenuItems();
        menuData.owner().openInventory(inventory);
    }

    public void close() {
        if (closing || inventory == null) return;
        closing = true;

        if (menuData.owner().getOpenInventory().getTopInventory() == inventory) menuData.owner().closeInventory();
        inventory = null;
    }
}