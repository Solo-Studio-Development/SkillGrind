package net.solostudio.skillgrind.listeners;

import net.solostudio.skillgrind.gui.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.handleMenu(event);
        }
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.onClose(event);
        }
    }
}
