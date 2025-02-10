package net.solostudio.skillgrind.listeners;

import net.solostudio.skillgrind.gui.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class MenuListener implements Listener {
    @EventHandler
    public void onClick(final @NotNull InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) menu.handleMenu(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(final @NotNull InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) menu.onClose(event);
    }
}
