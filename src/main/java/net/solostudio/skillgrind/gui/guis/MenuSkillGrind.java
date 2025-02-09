package net.solostudio.skillgrind.gui.guis;

import net.solostudio.skillgrind.gui.Menu;
import net.solostudio.skillgrind.item.ItemFactory;
import net.solostudio.skillgrind.data.MenuData;
import net.solostudio.skillgrind.processor.MessageProcessor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MenuSkillGrind extends Menu {
    private static final int[] BORDER_SLOTS = {0, 2, 3, 5, 6, 8};
    private static final int INPUT_SLOT = 1;
    private static final int ENCHANT_SLOT = 4;
    private static final int OUTPUT_SLOT = 7;

    private final LinkedHashMap<Enchantment, Integer> totalEnchants = new LinkedHashMap<>();
    private final LinkedHashMap<Enchantment, Integer> selectedEnchants = new LinkedHashMap<>();
    private int currentIndex = 0;

    public MenuSkillGrind(MenuData controller) {
        super(controller);
        initialize();
    }

    private void initialize() {
        setupBorder();
        setupInputSlot();
        setupEnchantBook();
        setupOutputSlot();
    }

    private void setupBorder() {
        ItemStack border = ItemFactory.create(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .finish();

        for (int slot : BORDER_SLOTS) {
            getInventory().setItem(slot, border);
        }
    }

    private void setupInputSlot() {
        getInventory().setItem(INPUT_SLOT, null);
    }

    private void setupEnchantBook() {
        updateEnchantBook();
    }

    private void setupOutputSlot() {
        getInventory().setItem(OUTPUT_SLOT, null);
    }

    @Override
    public String getMenuName() {
        return "&8Custom Grindstone";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public int getMenuTick() {
        return 0; // No auto-update needed
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getRawSlot();

        if (slot == INPUT_SLOT) handleInputClick(event);
        else if (slot == ENCHANT_SLOT) handleEnchantClick(event);
        else if (slot == OUTPUT_SLOT) handleOutputClick(event);
        else if (event.getClickedInventory() == player.getInventory()) handleInventoryClick(event);
    }

    private void handleInputClick(InventoryClickEvent event) {
        final ItemStack cursor = event.getCursor();
        final ItemStack current = event.getCurrentItem();

        if (!isAir(current)) {
            setInput(cursor);
            event.setCursor(current);
            playClickSound((Player) event.getWhoClicked());
            update();
        }
    }

    private void handleEnchantClick(InventoryClickEvent event) {
        if (totalEnchants.isEmpty()) return;

        final ClickType click = event.getClick();
        final Enchantment target = getCurrentEnchantment();

        if (click == ClickType.DROP) {
            toggleAllEnchants();
        } else if (click.isShiftClick()) {
            adjustEnchantLevel(target, click.isLeftClick());
        } else {
            navigateEnchants(click);
        }

        playClickSound((Player) event.getWhoClicked());
        update();
    }

    private void handleOutputClick(InventoryClickEvent event) {
        final ItemStack output = event.getCurrentItem();
        if (isAir(output)) return;

        final ItemStack input = getInput();
        applyOutputToInput(input, output);

        event.setCurrentItem(null);
        event.getWhoClicked().setItemOnCursor(output);
        update();
    }

    private void handleInventoryClick(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            final ItemStack clicked = event.getCurrentItem();
            if (!isAir(clicked) && isAir(getInput())) {
                setInput(clicked);
                event.setCurrentItem(null);
                playClickSound((Player) event.getWhoClicked());
                update();
            }
        }
    }

    private void setInput(ItemStack item) {
        totalEnchants.clear();
        selectedEnchants.clear();
        currentIndex = 0;

        getInventory().setItem(INPUT_SLOT, item);

        if (!isAir(item)) {
            if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                totalEnchants.putAll(meta.getStoredEnchants());
            } else {
                totalEnchants.putAll(item.getEnchantments());
            }
        }
    }

    private void updateEnchantBook() {
        ItemStack book = ItemFactory.create(Material.ENCHANTED_BOOK)
                .setName(MessageProcessor.process("&7Enchantment Control"))
                .addLore(generateEnchantLore())
                .finish();

        getInventory().setItem(ENCHANT_SLOT, book);
        updateOutput();
    }

    private String[] generateEnchantLore() {
        return totalEnchants.entrySet().stream()
                .map(entry -> {
                    String name = formatEnchantName(entry.getKey());
                    int baseLevel = entry.getValue();
                    int selectedLevel = selectedEnchants.getOrDefault(entry.getKey(), 0);

                    String color = (currentIndex == getEnchantPosition(entry.getKey())) ? "&a" : "&7";
                    return MessageProcessor.process(color + "⯈ " + name + " " + baseLevel + " &8→ &7" + selectedLevel);
                })
                .toArray(String[]::new);
    }

    private void updateOutput() {
        if (selectedEnchants.isEmpty()) {
            getInventory().setItem(OUTPUT_SLOT, null);
            return;
        }

        ItemStack output = ItemFactory.create(Material.ENCHANTED_BOOK).finish();
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) output.getItemMeta();

        selectedEnchants.forEach((enchant, level) -> {
            if (level > 0) meta.addStoredEnchant(enchant, level, true);
        });

        output.setItemMeta(meta);
        getInventory().setItem(OUTPUT_SLOT, output);
    }

    private void toggleAllEnchants() {
        if (selectedEnchants.isEmpty()) {
            totalEnchants.forEach((enchant, level) ->
                    selectedEnchants.put(enchant, level));
        } else {
            selectedEnchants.clear();
        }
    }

    private void adjustEnchantLevel(Enchantment enchant, boolean increase) {
        int change = increase ? 1 : -1;
        int newLevel = selectedEnchants.getOrDefault(enchant, 0) + change;
        newLevel = Math.max(0, Math.min(newLevel, totalEnchants.get(enchant)));

        if (newLevel == 0) {
            selectedEnchants.remove(enchant);
        } else {
            selectedEnchants.put(enchant, newLevel);
        }
    }

    private void navigateEnchants(ClickType click) {
        if (click == ClickType.LEFT) currentIndex++;
        else if (click == ClickType.RIGHT) currentIndex--;

        currentIndex = Math.floorMod(currentIndex, totalEnchants.size());
    }

    private void applyOutputToInput(ItemStack input, ItemStack output) {
        EnchantmentStorageMeta outputMeta = (EnchantmentStorageMeta) output.getItemMeta();

        if (input.getItemMeta() instanceof EnchantmentStorageMeta inputMeta) {
            outputMeta.getStoredEnchants().forEach((enchant, level) -> {
                int remaining = totalEnchants.get(enchant) - level;
                if (remaining > 0) inputMeta.addStoredEnchant(enchant, remaining, true);
                else inputMeta.removeStoredEnchant(enchant);
            });
            input.setItemMeta(inputMeta);
        } else {
            ItemMeta meta = input.getItemMeta();
            outputMeta.getStoredEnchants().forEach((enchant, level) -> {
                int remaining = totalEnchants.get(enchant) - level;
                if (remaining > 0) meta.addEnchant(enchant, remaining, true);
                else meta.removeEnchant(enchant);
            });
            input.setItemMeta(meta);
        }
    }

    private Enchantment getCurrentEnchantment() {
        return totalEnchants.keySet().stream()
                .skip(currentIndex)
                .findFirst()
                .orElse(null);
    }

    private int getEnchantPosition(Enchantment enchant) {
        int pos = 0;
        for (Enchantment e : totalEnchants.keySet()) {
            if (e.equals(enchant)) return pos;
            pos++;
        }
        return -1;
    }

    private String formatEnchantName(Enchantment enchant) {
        return enchant.getKey().getKey().replace("_", " ").toLowerCase();
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
    }

    @Override
    public void setMenuItems() {}

    private ItemStack getInput() {
        return getInventory().getItem(INPUT_SLOT);
    }

    private void update() {
        updateEnchantBook();
        updateOutput();
    }

    public void onClose() {
        ItemStack input = getInput();
        Player player = menuData.owner(); // Módosítva: direkt mező elérés

        if (!isAir(input)) {
            player.getInventory().addItem(input).values()
                    .forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
        }
    }
}
