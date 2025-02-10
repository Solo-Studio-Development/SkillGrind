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
import java.util.Map;

@SuppressWarnings("deprecation")
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
    public void handleMenu(@NotNull final InventoryClickEvent event) {
        event.setCancelled(true);
        handleClickActions(event);
    }

    private void handleClickActions(@NotNull final InventoryClickEvent event) {
        final int slot = event.getRawSlot();

        switch (slot) {
            case INPUT_SLOT -> handleInputTransfer(event);
            case ENCHANT_SLOT -> handleEnchantSelection(event);
            case OUTPUT_SLOT -> handleOutputExtraction(event);
            default -> handleInventoryInteractions(event);
        }
    }

    @Override
    public void setMenuItems() {
        Arrays.stream(BORDER_SLOTS).forEach(slot -> getInventory().setItem(slot, ItemKeys.FILLER_GLASS.getItem()));
    }

    @Override
    public void onClose(@NotNull final InventoryCloseEvent event) {
        if (event.getInventory().equals(getInventory())) {
            handleInventoryCleanup((Player) event.getPlayer());
            close();
        }
    }

    private void handleInventoryCleanup(@NotNull Player player) {
        ItemStack input = getInventory().getItem(INPUT_SLOT);
        ItemStack output = getInventory().getItem(OUTPUT_SLOT);

        if (input != null && !input.getType().isAir() && output == null) {
            player.getInventory().addItem(input)
                    .values()
                    .forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
        }

        getInventory().clear();
        GrindstoneUtils.playSound(player);
    }

    private void handleInputTransfer(@NotNull final InventoryClickEvent event) {
        final ItemStack cursor = event.getCursor();
        if (!GrindstoneUtils.isValidItem(cursor)) return;

        setInput(cursor);
        event.setCursor(null);
        GrindstoneUtils.playSound((Player) event.getWhoClicked());
        update();
    }

    private void setInput(@NotNull ItemStack item) {
        clearEnchantmentState();
        getInventory().setItem(INPUT_SLOT, item);

        if (GrindstoneUtils.isValidItem(item)) {
            var meta = item.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta storageMeta) totalEnchants.putAll(storageMeta.getStoredEnchants());
            else totalEnchants.putAll(item.getEnchantments());
        }
        update();
    }

    private void handleEnchantSelection(@NotNull final InventoryClickEvent event) {
        if (totalEnchants.isEmpty()) return;

        switch (event.getClick()) {
            case DROP -> toggleAllEnchants();
            case SHIFT_LEFT, SHIFT_RIGHT -> adjustEnchantLevel(event.getClick().isLeftClick());
            default -> navigateEnchants(event.getClick());
        }
        GrindstoneUtils.playSound((Player) event.getWhoClicked());
        update();
    }

    private void toggleAllEnchants() {
        selectedEnchants.putAll(selectedEnchants.isEmpty() ? totalEnchants : Map.of());
    }

    private void adjustEnchantLevel(boolean increase) {
        var enchant = getCurrentEnchantment();
        if (enchant == null) return;

        int newLevel = selectedEnchants.getOrDefault(enchant, 0) + (increase ? 1 : -1);
        newLevel = Math.clamp(newLevel, 0, totalEnchants.get(enchant));

        if (newLevel > 0) selectedEnchants.put(enchant, newLevel);
        else selectedEnchants.remove(enchant);
    }

    private void navigateEnchants(@NotNull ClickType click) {
        currentIndex += switch (click) {
            case LEFT -> 1;
            case RIGHT -> -1;
            default -> 0;
        };

        currentIndex = Math.floorMod(currentIndex, totalEnchants.size());
    }

    private void handleOutputExtraction(@NotNull final InventoryClickEvent event) {
        if (event.getClick().isShiftClick()) return;

        final ItemStack output = event.getCurrentItem();
        if (!GrindstoneUtils.isValidItem(output)) return;
        if (output == null) return;

        modifyInputItem(output);
        transferOutput(event, output);

        final ItemStack modifiedInput = getInventory().getItem(INPUT_SLOT);
        if (modifiedInput != null && !modifiedInput.getType().isAir()) {
            final Player player = (Player) event.getWhoClicked();
            final Map<Integer, ItemStack> leftovers = player.getInventory().addItem(modifiedInput.clone());
            leftovers.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
            getInventory().setItem(INPUT_SLOT, null);
        }

        clearEnchantmentState();
        getInventory().setItem(OUTPUT_SLOT, null);
        closeInventorySafely((Player) event.getWhoClicked());
    }

    private void closeInventorySafely(@NotNull Player player) {
        update();
        player.closeInventory();
    }

    private void modifyInputItem(@NotNull ItemStack output) {
        ItemStack input = getInventory().getItem(INPUT_SLOT);
        if (input == null || input.getType().isAir()) return;

        ItemStack modifiedInput = input.clone();
        ItemMeta inputMeta = modifiedInput.getItemMeta();

        EnchantmentStorageMeta outputMeta = (EnchantmentStorageMeta) output.getItemMeta();
        if (outputMeta == null) return;

        outputMeta.getStoredEnchants().forEach((enchant, level) -> {
            int remaining = totalEnchants.getOrDefault(enchant, 0) - level;

            if (inputMeta instanceof EnchantmentStorageMeta storageMeta) handleEnchantedBook(storageMeta, enchant, remaining);
            else handleRegularItem(inputMeta, enchant, remaining);
        });

        modifiedInput.setItemMeta(inputMeta);
        getInventory().setItem(INPUT_SLOT, modifiedInput);
    }

    private void handleEnchantedBook(@NotNull EnchantmentStorageMeta meta, @NotNull Enchantment enchant, int remaining) {
        meta.removeStoredEnchant(enchant);
        if (remaining > 0) meta.addStoredEnchant(enchant, remaining, true);
    }

    private void handleRegularItem(@NotNull ItemMeta meta, @NotNull Enchantment enchant, int remaining) {
        meta.removeEnchant(enchant);
        if (remaining > 0) meta.addEnchant(enchant, remaining, true);
    }

    private void transferOutput(@NotNull InventoryClickEvent event, ItemStack output) {
        var player = (Player) event.getWhoClicked();

        event.setCurrentItem(null);
        event.setCancelled(true);

        player.setItemOnCursor(output);
    }

    private void handleInventoryInteractions(@NotNull final InventoryClickEvent event) {
        if (event.isShiftClick() && event.getClickedInventory() == event.getView().getBottomInventory()) {
            ItemStack clicked = event.getCurrentItem();

            if (clicked == null) return;

            if (GrindstoneUtils.isValidItem(clicked) && !GrindstoneUtils.isValidItem(getInput())) {
                setInput(clicked);
                event.setCurrentItem(null);
                GrindstoneUtils.playSound((Player) event.getWhoClicked());
                update();
            }
        }
    }

    private void update() {
        updateEnchantBook();
        updateOutputDisplay();
    }

    private void updateEnchantBook() {
        var book = ItemFactory.create(Material.ENCHANTED_BOOK)
                .setName(ConfigKeys.GRINDSTONE_DEFAULT_BOOK_NAME.getString())
                .addLore(createEnchantLore())
                .finish();

        getInventory().setItem(ENCHANT_SLOT, book);
    }

    private String @NotNull [] createEnchantLore() {
        return totalEnchants.entrySet().stream()
                .map(entry -> {
                    var enchant = entry.getKey();
                    String name = formatEnchantName(enchant);
                    int baseLevel = entry.getValue();
                    int selectedLevel = selectedEnchants.getOrDefault(enchant, 0);
                    String color = (currentIndex == getEnchantPosition(enchant)) ? ConfigKeys.GRINDSTONE_SELECTED_ENCHANT_COLOR.getString() : ConfigKeys.GRINDSTONE_UNSELECTED_ENCHANT_COLOR.getString();

                    return MessageProcessor.process(("%s" + ConfigKeys.GRINDSTONE_BEFORE_SYMBOL.getString() + "%s %s &8" + ConfigKeys.GRINDSTONE_AFTER_SYMBOL.getString() + "&7%s").formatted(color, name, baseLevel, selectedLevel));
                })
                .toArray(String[]::new);
    }

    private void updateOutputDisplay() {
        if (selectedEnchants.isEmpty()) {
            getInventory().setItem(OUTPUT_SLOT, null);
            return;
        }

        var output = ItemFactory.create(Material.ENCHANTED_BOOK).finish();
        var meta = (EnchantmentStorageMeta) output.getItemMeta();

        selectedEnchants.forEach((enchant, level) -> meta.addStoredEnchant(enchant, level, true));
        output.setItemMeta(meta);
        getInventory().setItem(OUTPUT_SLOT, output);
    }

    private @Nullable Enchantment getCurrentEnchantment() {
        return totalEnchants.keySet().stream()
                .skip(currentIndex)
                .findFirst()
                .orElse(null);
    }

    private int getEnchantPosition(@NotNull Enchantment enchant) {
        int position = 0;

        for (var enchantment : totalEnchants.keySet()) {
            if (enchantment.equals(enchant)) return position;
            position++;
        }

        return -1;
    }

    private @NotNull String formatEnchantName(@NotNull Enchantment enchant) {
        return enchant.getKey().getKey().replace('_', ' ').toLowerCase();
    }

    private ItemStack getInput() {
        return getInventory().getItem(INPUT_SLOT);
    }

    private void clearEnchantmentState() {
        totalEnchants.clear();
        selectedEnchants.clear();
        currentIndex = 0;
        update();
    }
}