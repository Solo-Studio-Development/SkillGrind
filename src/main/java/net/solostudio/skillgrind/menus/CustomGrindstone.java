package net.solostudio.skillgrind.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.solostudio.skillgrind.ItemCreator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Stream;

public class CustomGrindstone extends Menu {
    private final LinkedHashMap<Enchantment, Integer> totalEnchants = new LinkedHashMap<>();
    private final HashMap<Enchantment, Integer> selectedEnchants = new HashMap<>();
    private int index;

    private final int enchantsIndex = 4;
    private final int inputIndex = 1;
    private final int outputIndex = 7;

    public CustomGrindstone() {
        super(9, Component.text("Grindstone", NamedTextColor.GRAY));
        setItem(0, ItemCreator.create(Material.BLACK_STAINED_GLASS_PANE, Component.empty()));
        setItem(2, ItemCreator.create(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));
        setItem(3, ItemCreator.create(Material.BLACK_STAINED_GLASS_PANE, Component.empty()));
        setItem(5, ItemCreator.create(Material.BLACK_STAINED_GLASS_PANE, Component.empty()));
        setItem(6, ItemCreator.create(Material.GRAY_STAINED_GLASS_PANE, Component.empty()));
        setItem(8, ItemCreator.create(Material.BLACK_STAINED_GLASS_PANE, Component.empty()));

        setItem(inputIndex, null, (p, event) -> {
            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();

            if (isAirOrNull(clicked)) return;

            setInput((cursor));
            p.setItemOnCursor(clicked);
            clickSound(p);
        });

        setItem(enchantsIndex, defaultEnchantBook(), (p, event) -> {
            ItemStack clicked = event.getCurrentItem();
            ItemStack input = getInput();

            if (isAirOrNull(clicked) || isAirOrNull(input)) return;
            ItemMeta meta = clicked.getItemMeta();

            if (meta == null || totalEnchants.isEmpty()) return;

            if (event.isShiftClick()) {
                if (index < 0 || index > totalEnchants.size() - 1) return;
                Enchantment selectedEnchant = getSelectedEnchant();

                if (selectedEnchant == null) return;

                int add = event.isLeftClick() ? 1 : -1;
                int level = wrapInt(getSelectedLevel() + add, 0, totalEnchants.get(selectedEnchant));

                if (level <= 0) selectedEnchants.remove(selectedEnchant);
                else selectedEnchants.put(selectedEnchant, level);
                update();
                clickSound(p);
            }

            if (event.getClick() == ClickType.DROP) {
                if (selectedEnchants.isEmpty()) selectedEnchants.putAll(totalEnchants);
                else selectedEnchants.clear();

                update();
                clickSound(p);
                return;
            }

            if (event.getClick() == ClickType.LEFT) index++;
            else if (event.getClick() == ClickType.RIGHT) index--;
            index = wrapInt(index, 0, totalEnchants.size() - 1);
            update();
            clickSound(p);
        });

        setItem(outputIndex, null, (p, event) -> {
            ItemStack clicked = event.getCurrentItem();

            if (isAirOrNull(clicked)) return;

            Map<Enchantment, Integer> enchants = ((EnchantmentStorageMeta) clicked.getItemMeta()).getStoredEnchants();

            if (enchants.isEmpty()) return;

            ItemStack input = getInput();

            if (isAirOrNull(input)) return;
            ItemMeta meta = input.getItemMeta();
            EnchantmentStorageMeta bookMeta = (meta instanceof EnchantmentStorageMeta) ? (EnchantmentStorageMeta) meta : null;

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                int totalLevel = totalEnchants.get(enchantment);

                if (bookMeta == null) {
                    meta.removeEnchant(enchantment);
                    if (level != totalLevel) {
                        meta.addEnchant(enchantment, totalLevel - level, true);
                    }
                } else {
                    bookMeta.removeStoredEnchant(enchantment);

                    if (level != totalLevel) {
                        bookMeta.addStoredEnchant(enchantment, totalLevel - level, true);
                    }
                }
            }

            input.setItemMeta(meta);

            if (!isAirOrNull(event.getCursor()) || event.getClick().isShiftClick()) {
                p.getInventory().addItem(clicked).forEach((i, item) -> {
                    p.getWorld().dropItem(p.getLocation(), item);
                });
            } else p.setItemOnCursor(clicked);

            if (bookMeta == null) enchants = meta.getEnchants();
            else enchants = bookMeta.getStoredEnchants();

            totalEnchants.clear();
            selectedEnchants.clear();
            totalEnchants.putAll(enchants);
            index = wrapInt(index, 0, totalEnchants.size() - 1);

            getInventory().setItem(outputIndex, null);
            update();
            clickSound(p);
        });

        setGeneralInvClickAction((p, event) -> {
            if (event.getClick() == ClickType.DOUBLE_CLICK) return;

            if (event.getClick().isShiftClick()) {
                ItemStack clicked = event.getCurrentItem();
                if (!isAirOrNull(getInput()) || isAirOrNull(clicked)) return;
                setInput(clicked);
                p.getInventory().setItem(event.getSlot(), null);
                clickSound(p);
                return;
            }
            event.setCancelled(true);
        });
        setGeneralDragAction((p, event) -> {
            for (int i : event.getRawSlots()) {
                if (i < getInventory().getSize()) return;
            }
            event.setCancelled(true);
        });

        setCloseAction(p -> {
            if (isAirOrNull(getInput())) return;

            HashMap<Integer, ItemStack> items = p.getInventory().addItem(getInput());
            items.forEach(((integer, itemStack) -> p.getWorld().dropItem(p.getLocation(), itemStack)));
        });
    }

    private boolean isAirOrNull(ItemStack item) {
        return item != null;
    }

    private int wrapInt(int value, int min, int max) {
        if (value < min) return max;
        if (value > max) return min;

        return value;
    }

    private String toTitleCase(String string) {
        if (string == null || string.isEmpty()) return string;

        return Stream.of(string.replaceAll("_", " ").split(" "))
                .map(s -> s.toUpperCase().charAt(0) + s.toLowerCase().substring(1))
                .reduce((s, s2) -> s + " " + s2).orElse("");
    }

    private ItemStack getInput() {
        return getInventory().getItem(inputIndex);
    }

    private void clickSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, .8f);
    }

    private ItemStack defaultEnchantBook() {
        return ItemCreator.create(Material.ENCHANTED_BOOK, Component.text("Enchantment List")
                .decoration(TextDecoration.ITALIC, false), null, true);
    }

    private Enchantment getSelectedEnchant() {
        int x = -1;

        for (Map.Entry<Enchantment, Integer> entry : totalEnchants.entrySet()) {
            x++;
            if (x == index) return entry.getKey();
        }
        return null;
    }

    private int getSelectedLevel() {
        return selectedEnchants.getOrDefault(getSelectedEnchant(), 0);
    }

    private void update() {
        ItemStack input = getInput();
        ItemStack output = null;

        if (isAirOrNull(input)) {
            selectedEnchants.clear();
            totalEnchants.clear();
            index = 0;
        } else {
            if (!selectedEnchants.isEmpty()) {
                output = ItemCreator.create(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) output.getItemMeta();

                selectedEnchants.forEach((enchantment, integer) -> meta.addStoredEnchant(enchantment, integer, true));
                output.setItemMeta(meta);
            }
        }

        setItem(outputIndex, output);
        updateEnchantBook();
    }

    private void setInput(ItemStack input) {
        totalEnchants.clear();
        selectedEnchants.clear();
        index = 0;
        setItem(inputIndex, input);

        if (!isAirOrNull(input) && input.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            totalEnchants.putAll(meta.getStoredEnchants());
        } else totalEnchants.putAll(input.getEnchantments());
        update();
    }

    private void updateEnchantBook() {
        ItemStack book = defaultEnchantBook();

        if (totalEnchants.isEmpty()) {
            setItem(enchantsIndex, book);
            return;
        }

        List<Component> lore = new ArrayList<>();
        Component c;
        int i = -1;
        Enchantment selectedEnchant = getSelectedEnchant();

        for (Map.Entry<Enchantment, Integer> entry : totalEnchants.entrySet()) {
            if (selectedEnchants.containsKey(entry.getKey())) {
                if (selectedEnchant != null && selectedEnchant == entry.getKey()) {
                    c = c.append(Component.text("⏵ ", NamedTextColor.WHITE));
                } else c = c.append(Component.text("• ", NamedTextColor.WHITE));
            } else {
                c = c.append(Component.text("⏵ ", NamedTextColor.GRAY));
            }

            c = c.append(Component.text(toTitleCase(entry.getKey().getKey().getKey()) + " " + entry.getValue(),
                    NamedTextColor.GRAY));

            if (selectedEnchants.containsKey(entry.getKey())) {
                c = c.append(Component.text(" » " + selectedEnchants.get(entry.getKey()), NamedTextColor.GRAY));
            }

            lore.add(c);
        }

        ItemMeta meta = book.getItemMeta();
        meta.lore(lore);
        book.setItemMeta(meta);
        setItem(enchantsIndex, book);
    }
}
