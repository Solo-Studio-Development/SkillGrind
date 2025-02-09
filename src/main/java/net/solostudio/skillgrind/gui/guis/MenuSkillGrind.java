package net.solostudio.skillgrind.gui.guis;

import net.solostudio.skillgrind.data.MenuData;
import net.solostudio.skillgrind.enums.keys.ConfigKeys;
import net.solostudio.skillgrind.enums.keys.ItemKeys;
import net.solostudio.skillgrind.gui.Menu;
import net.solostudio.skillgrind.item.ItemFactory;
import net.solostudio.skillgrind.processor.MessageProcessor;
import net.solostudio.skillgrind.utils.GrindstoneUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;

@SuppressWarnings("all")
public class MenuSkillGrind extends Menu {
    private static final int[] BORDER_SLOTS = {0, 2, 3, 5, 6, 8};
    private static final int INPUT_SLOT = 1;
    private static final int ENCHANT_SLOT = 4;
    private static final int OUTPUT_SLOT = 7;

    private final LinkedHashMap<Enchantment, Integer> totalEnchants = new LinkedHashMap<>();
    private final LinkedHashMap<Enchantment, Integer> selectedEnchants = new LinkedHashMap<>();
    private int currentIndex = 0;

    public MenuSkillGrind(@NotNull MenuData menuData) {
        super(menuData);
    }

    @Override
    public String getMenuName() {
        return ConfigKeys.GRINDSTONE_TITLE.getString();
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getRawSlot();

        // Csak a GUI interakciókat blokkoljuk
        if (event.getClickedInventory() == inventory) {
            event.setCancelled(true);
            if (slot == INPUT_SLOT) handleInputClick(event);
            else if (slot == ENCHANT_SLOT) handleEnchantClick(event);
            else if (slot == OUTPUT_SLOT) handleOutputClick(event);
        } else if (event.isShiftClick()) {
            handleInventoryClick(event);
        }
    }

    @Override
    public void setMenuItems() {
        Arrays.stream(BORDER_SLOTS).forEach(slot -> {
           getInventory().setItem(slot, ItemKeys.FILLER_GLASS.getItem());
        });
    }

    @Override
    public void onClose(final InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            getInventory().setItem(INPUT_SLOT, null);
            getInventory().setItem(OUTPUT_SLOT, null);
            close();
        }
    }

    private void handleInputClick(InventoryClickEvent event) {
        final ItemStack cursor = event.getCursor();
        final ItemStack current = event.getCurrentItem();

        if (!GrindstoneUtils.isValidItem(current) && GrindstoneUtils.isValidItem(cursor)) {
            setInput(cursor);
            event.setCursor(current);
            GrindstoneUtils.playSound((Player) event.getWhoClicked());
            update();
        }
    }

    private void setInput(@Nullable ItemStack item) {
        totalEnchants.clear();
        selectedEnchants.clear();
        currentIndex = 0;

        getInventory().setItem(INPUT_SLOT, item);

        if (GrindstoneUtils.isValidItem(item)) {
            if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) totalEnchants.putAll(meta.getStoredEnchants());
            else totalEnchants.putAll(item.getEnchantments());
        }
    }

    private void handleEnchantClick(final InventoryClickEvent event) {
        if (totalEnchants.isEmpty()) return;

        final ClickType click = event.getClick();
        final Enchantment target = getCurrentEnchantment();

        switch (click) {
            case DROP -> toggleAllEnchants();
            case SHIFT_LEFT, SHIFT_RIGHT -> adjustEnchantLevel(target, click.isLeftClick());
            default -> navigateEnchants(click);
        }

        GrindstoneUtils.playSound((Player) event.getWhoClicked());
        update();
    }

    private void handleOutputClick(final InventoryClickEvent event) {
        final ItemStack output = event.getCurrentItem();
        if (output == null || !GrindstoneUtils.isValidItem(output)) return;

        final ItemStack input = getInput();
        applyOutputToInput(input, output); // Módosítja az inputot

        // Töröld az outputot és helyezd a kurzorra
        event.setCurrentItem(null);
        event.getWhoClicked().setItemOnCursor(output);

        // Frissítsd az inputot a GUI-ban (fontos!)
        getInventory().setItem(INPUT_SLOT, input);

        // Ha az input elvesztette az összes varázslatot, töröld!
        if (!GrindstoneUtils.isValidItem(input)) {
            getInventory().setItem(INPUT_SLOT, null);
        }

        update();
    }

    private void handleInventoryClick(final InventoryClickEvent event) {
        if (event.isShiftClick()) {
            final ItemStack clicked = event.getCurrentItem();

            if (GrindstoneUtils.isValidItem(clicked) && !GrindstoneUtils.isValidItem(getInput())) {
                setInput(clicked);
                event.setCurrentItem(null);
                GrindstoneUtils.playSound((Player) event.getWhoClicked());
                update();
            }
        }
    }

    private void updateEnchantBook() {
        ItemStack book = ItemFactory.create(Material.ENCHANTED_BOOK)
                .setName(ConfigKeys.GRINDSTONE_DEFAULT_BOOK_NAME.getString())
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
        if (selectedEnchants.isEmpty()) totalEnchants.forEach((enchant, level) -> selectedEnchants.put(enchant, level));
        else selectedEnchants.clear();
    }

    private void adjustEnchantLevel(@NotNull Enchantment enchant, boolean increase) {
        int change = increase ? 1 : -1;
        int newLevel = selectedEnchants.getOrDefault(enchant, 0) + change;
        newLevel = Math.max(0, Math.min(newLevel, totalEnchants.get(enchant)));

        if (newLevel == 0) selectedEnchants.remove(enchant);
        else selectedEnchants.put(enchant, newLevel);
    }

    private void navigateEnchants(@NotNull ClickType click) {
        if (click == ClickType.LEFT) currentIndex++;
        else if (click == ClickType.RIGHT) currentIndex--;

        currentIndex = Math.floorMod(currentIndex, totalEnchants.size());
    }

    private void applyOutputToInput(@NotNull ItemStack input, @NotNull ItemStack output) {
        if (input == null || output == null) return;

        EnchantmentStorageMeta outputMeta = (EnchantmentStorageMeta) output.getItemMeta();

        if (outputMeta == null) return;

        if (input.getItemMeta() instanceof EnchantmentStorageMeta inputMeta) {
            outputMeta.getStoredEnchants().forEach((enchant, level) -> {
                if (!totalEnchants.containsKey(enchant)) return;

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

        if (!GrindstoneUtils.isValidItem(input)) {
            getInventory().setItem(INPUT_SLOT, null); // Töröld, ha nem érvényes
        }

        update();
    }

    private Enchantment getCurrentEnchantment() {
        return totalEnchants.keySet().stream()
                .skip(currentIndex)
                .findFirst()
                .orElse(null);
    }

    private int getEnchantPosition(@NotNull Enchantment enchant) {
        int pos = 0;
        for (Enchantment e : totalEnchants.keySet()) {
            if (e.equals(enchant)) return pos;
            pos++;
        }
        return -1;
    }

    private @NotNull String formatEnchantName(@NotNull Enchantment enchant) {
        return enchant.getKey().getKey().replace("_", " ").toLowerCase();
    }

    private ItemStack getInput() {
        return getInventory().getItem(INPUT_SLOT);
    }

    private void update() {
        updateEnchantBook();
        updateOutput();
    }
}
