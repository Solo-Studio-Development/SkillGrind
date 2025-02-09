package net.solostudio.skillgrind.listeners;

import net.solostudio.skillgrind.data.MenuData;
import net.solostudio.skillgrind.gui.guis.MenuSkillGrind;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class GeneralEvents implements Listener {
    @EventHandler
    public void onGrindstoneOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.GRINDSTONE
                && event.getPlayer() instanceof Player player
                && player.isSneaking()) {

            event.setCancelled(true);
            new MenuSkillGrind(MenuData.getMenuUtils(player)).open();
        }
    }
}
