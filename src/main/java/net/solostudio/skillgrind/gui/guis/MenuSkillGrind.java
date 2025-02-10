package net.solostudio.skillgrind.gui.guis;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.data.MenuData;
import net.solostudio.skillgrind.enums.keys.ConfigKeys;
import net.solostudio.skillgrind.enums.keys.ItemKeys;
import net.solostudio.skillgrind.gui.Menu;
import net.solostudio.skillgrind.handlers.EnchantHandler;
import net.solostudio.skillgrind.processor.MessageProcessor;
import net.solostudio.skillgrind.utils.GrindstoneUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public final class MenuSkillGrind extends Menu {
    private static final int[] BORDER_SLOTS = {0, 2, 3, 5, 6, 8};
    private static final int INPUT_SLOT = 1;
    private static final int ENCHANT_SLOT = 4;
    private static final int OUTPUT_SLOT = 7;

    private final EnchantHandler enchantHandler = SkillGrind.getInstance().getEnchantHandler();
    private final ItemStack cachedEnchantedBook = enchantHandler.createBaseBook();

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
        if (!event.getInventory().equals(getInventory())) return;

        switch (event.getRawSlot()) {
            case INPUT_SLOT -> handleInputTransfer(event);
            case ENCHANT_SLOT -> handleEnchantInteraction(event);
            case OUTPUT_SLOT -> handleOutputExtraction(event);
            default -> handleInventoryInteractions(event);
        }
    }

    @Override
    public void setMenuItems() {
        Arrays.stream(BORDER_SLOTS).forEach(slot -> getInventory().setItem(slot, ItemKeys.FILLER_GLASS.getItem()));
        updateEnchantDisplay();
    }

    @Override
    public void onClose(@NotNull final InventoryCloseEvent event) {
        if (!event.getInventory().equals(getInventory())) return;

        final Player player = (Player) event.getPlayer();
        handleInventoryCleanup(player);
        GrindstoneUtils.playSound(player);
        close();
    }

    private void handleInventoryCleanup(@NotNull Player player) {
        Optional.ofNullable(getInventory().getItem(INPUT_SLOT))
                .filter(item -> !item.getType().isAir())
                .ifPresent(item -> {
                    if (getInventory().getItem(OUTPUT_SLOT) == null) {
                        GrindstoneUtils.safeGiveItem(player, item);
                    }
                });

        getInventory().clear();
    }

    private void handleInputTransfer(@NotNull final InventoryClickEvent event) {
        Optional.of(event.getCursor())
                .filter(GrindstoneUtils::isValidItem)
                .ifPresent(item -> {
                    setInput(item);
                    event.setCursor(null);
                    GrindstoneUtils.playSound((Player) event.getWhoClicked());
                });
    }

    private void setInput(@NotNull ItemStack item) {
        enchantHandler.clear();
        getInventory().setItem(INPUT_SLOT, item);
        enchantHandler.extractEnchantments(item);
        updateEnchantDisplay();
    }

    private void handleEnchantInteraction(@NotNull final InventoryClickEvent event) {
        if (enchantHandler.isEmpty()) return;

        switch (event.getClick()) {
            case DROP -> enchantHandler.toggleAllEnchants();
            case SHIFT_LEFT -> adjustEnchantLevel(true);
            case SHIFT_RIGHT -> adjustEnchantLevel(false);
            case LEFT -> enchantHandler.navigate(true);
            case RIGHT -> enchantHandler.navigate(false);
            default -> {}
        }

        GrindstoneUtils.playSound((Player) event.getWhoClicked());
        updateEnchantDisplay();
    }

    private void adjustEnchantLevel(boolean increase) {
        enchantHandler.currentEnchantment().ifPresent(enchant -> enchantHandler.adjustEnchantLevel(enchant, increase));
    }

    private void handleOutputExtraction(@NotNull final InventoryClickEvent event) {
        if (event.getClick().isShiftClick()) return;

        Optional.ofNullable(event.getCurrentItem())
                .filter(GrindstoneUtils::isValidItem)
                .ifPresent(output -> {
                    modifyInputItem(output);
                    transferOutput(event, output);
                    cleanupAfterExtraction((Player) event.getWhoClicked());
                });
    }

    private void modifyInputItem(@NotNull ItemStack output) {
        Optional.ofNullable(getInventory().getItem(INPUT_SLOT))
                .filter(input -> !input.getType().isAir())
                .ifPresent(input -> {
                    ItemStack modifiedInput = processEnchantRemoval(input, output);
                    getInventory().setItem(INPUT_SLOT, modifiedInput);
                });
    }

    private @NotNull ItemStack processEnchantRemoval(@NotNull ItemStack input, @NotNull ItemStack output) {
        ItemStack modifiedInput = input.clone();
        ItemMeta meta = modifiedInput.getItemMeta();
        EnchantmentStorageMeta outputMeta = (EnchantmentStorageMeta) output.getItemMeta();

        outputMeta.getStoredEnchants().forEach((enchant, level) -> updateEnchantLevel(meta, enchant, level));
        modifiedInput.setItemMeta(meta);
        return modifiedInput;
    }

    private void updateEnchantLevel(@NotNull ItemMeta meta, @NotNull Enchantment enchant, int level) {
        int remaining = enchantHandler.getTotalLevel(enchant) - level;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchant);
            if (remaining > 0) storageMeta.addStoredEnchant(enchant, remaining, true);
        } else {
            meta.removeEnchant(enchant);
            if (remaining > 0) meta.addEnchant(enchant, remaining, true);
        }
    }

    private void transferOutput(@NotNull final InventoryClickEvent event, @NotNull ItemStack output) {
        event.setCurrentItem(null);
        event.getWhoClicked().setItemOnCursor(output);
    }

    private void cleanupAfterExtraction(@NotNull Player player) {
        Optional.ofNullable(getInventory().getItem(INPUT_SLOT))
                .filter(input -> !input.getType().isAir())
                .ifPresent(input -> GrindstoneUtils.safeGiveItem(player, input));

        enchantHandler.clear();
        getInventory().setItem(INPUT_SLOT, null);
        getInventory().setItem(OUTPUT_SLOT, null);
        player.closeInventory();
    }

    private void handleInventoryInteractions(@NotNull final InventoryClickEvent event) {
        if (event.isShiftClick() && event.getClickedInventory() == event.getView().getBottomInventory()) {
            Optional.ofNullable(event.getCurrentItem())
                    .filter(item -> GrindstoneUtils.isValidItem(item) && !GrindstoneUtils.isValidItem(getInput()))
                    .ifPresent(item -> {
                        setInput(item);
                        event.setCurrentItem(null);
                        GrindstoneUtils.playSound((Player) event.getWhoClicked());
                    });
        }
    }

    private void updateEnchantDisplay() {
        ItemStack displayBook = cachedEnchantedBook.clone();
        ItemMeta meta = displayBook.getItemMeta();

        meta.setLore(enchantHandler.getTotalEnchants().entrySet().stream()
                .map(this::formatEnchantLine)
                .toList());

        displayBook.setItemMeta(meta);
        getInventory().setItem(ENCHANT_SLOT, displayBook);
        updateOutput();
    }

    private @NotNull String formatEnchantLine(Map.@NotNull Entry<Enchantment, Integer> entry) {
        return MessageProcessor.process(
                "%s%s%s %s&8%s&7%s".formatted(
                        getEnchantColor(entry.getKey()),
                        ConfigKeys.GRINDSTONE_BEFORE_SYMBOL.getString(),
                        enchantHandler.getFormattedName(entry.getKey()),
                        entry.getValue(),
                        ConfigKeys.GRINDSTONE_AFTER_SYMBOL.getString(),
                        enchantHandler.getSelectedLevel(entry.getKey())
                )
        );
    }

    private String getEnchantColor(@NotNull Enchantment enchant) {
        return enchantHandler.currentEnchantment()
                .filter(current -> current.equals(enchant))
                .map(selected -> ConfigKeys.GRINDSTONE_SELECTED_ENCHANT_COLOR.getString())
                .orElse(ConfigKeys.GRINDSTONE_UNSELECTED_ENCHANT_COLOR.getString());
    }

    private void updateOutput() {
        if (enchantHandler.hasSelectedEnchants()) getInventory().setItem(OUTPUT_SLOT, createOutputBook());
        else getInventory().setItem(OUTPUT_SLOT, null);
    }

    private @NotNull ItemStack createOutputBook() {
        ItemStack output = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) output.getItemMeta();

        enchantHandler.getSelectedEnchants().forEach((enchant, level) -> meta.addStoredEnchant(enchant, level, true));
        output.setItemMeta(meta);
        return output;
    }

    private ItemStack getInput() {
        return getInventory().getItem(INPUT_SLOT);
    }
}