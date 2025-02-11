package net.solostudio.skillgrind.handlers;

import lombok.Getter;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.cache.NameCache;
import net.solostudio.skillgrind.enums.keys.ConfigKeys;
import net.solostudio.skillgrind.enums.keys.MessageKeys;
import net.solostudio.skillgrind.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EnchantHandler {
    private final LinkedHashMap<Enchantment, Integer> totalEnchants = new LinkedHashMap<>();
    private final Map<Enchantment, Integer> selectedEnchants = new ConcurrentHashMap<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public void extractEnchantments(ItemStack item) {
        clear();
        if (item == null || item.getType().isAir()) return;

        Map<Enchantment, Integer> enchants = (item.getItemMeta() instanceof EnchantmentStorageMeta meta) ? meta.getStoredEnchants() : item.getEnchantments();

        enchants.forEach((enchant, level) -> {
            if (enchant.getMaxLevel() >= level) totalEnchants.put(enchant, level);
        });
    }

    public void toggleAllEnchants() {
        if (selectedEnchants.isEmpty()) selectedEnchants.putAll(totalEnchants);
        else selectedEnchants.clear();
    }

    public void adjustEnchantLevel(Enchantment enchant, boolean increase) {
        int newLevel = selectedEnchants.getOrDefault(enchant, 0) + (increase ? 1 : -1);
        newLevel = Math.clamp(newLevel, 0, totalEnchants.getOrDefault(enchant, 0));

        if (newLevel > 0) selectedEnchants.put(enchant, newLevel);
        else selectedEnchants.remove(enchant);
    }

    public void navigate(boolean forward) {
        currentIndex.updateAndGet(i -> Math.floorMod(i + (forward ? 1 : -1), totalEnchants.size()));
    }

    public Optional<Enchantment> currentEnchantment() {
        return Optional.ofNullable(getByIndex(currentIndex.get()));
    }

    public @Unmodifiable Map<Enchantment, Integer> getSelectedEnchants() {
        return Collections.unmodifiableMap(selectedEnchants);
    }

    public @Unmodifiable Map<Enchantment, Integer> getTotalEnchants() {
        return Collections.unmodifiableMap(totalEnchants);
    }

    public String getFormattedName(Enchantment enchant) {
        return SkillGrind.getInstance().getNameCache().get(enchant);
    }

    public void clear() {
        totalEnchants.clear();
        selectedEnchants.clear();
        currentIndex.set(0);
    }

    private @Nullable Enchantment getByIndex(int index) {
        Iterator<Enchantment> it = totalEnchants.keySet().iterator();

        for (int i = 0; it.hasNext() && i <= index; i++) {
            Enchantment enchant = it.next();
            if (i == index) return enchant;
        }

        return null;
    }

    public boolean isEmpty() {
        return totalEnchants.isEmpty();
    }

    public int getSelectedLevel(Enchantment enchant) {
        return selectedEnchants.getOrDefault(enchant, 0);
    }

    public boolean hasSelectedEnchants() {
        return !selectedEnchants.isEmpty();
    }

    public int getTotalLevel(Enchantment enchant) {
        return totalEnchants.getOrDefault(enchant, 0);
    }

    public ItemStack createBaseBook() {
        return ItemFactory.create(Material.ENCHANTED_BOOK)
                .setName(ConfigKeys.GRINDSTONE_DEFAULT_BOOK_NAME.getString())
                .finish();
    }

    public boolean canAffordLevelReduction(Player player, int levelsToRemove) {
        int requiredLevels = levelsToRemove * ConfigKeys.GRINDSTONE_PER_XP_PER_LEVEL.getInt();
        int playerLevel = player.getLevel();
        float playerExp = player.getExp(); // XP pontok 0.0 és 1.0 között

        // Számítsuk ki a teljes XP mennyiséget (szintek + XP pontok)
        double totalPlayerXP = playerLevel + playerExp;

        if (totalPlayerXP >= requiredLevels) {
            // Levonjuk a szükséges XP-t
            double remainingXP = totalPlayerXP - requiredLevels;
            player.setLevel((int) remainingXP); // Beállítjuk az új szintet
            player.setExp((float) (remainingXP - (int) remainingXP)); // Beállítjuk az XP pontokat
            return true;
        } else {
            return false;
        }
    }

    public void sendNotEnoughLevelsMessage(@NotNull Player player, int levelsToRemove) {
        int requiredLevels = levelsToRemove * ConfigKeys.GRINDSTONE_PER_XP_PER_LEVEL.getInt();
        player.sendMessage(MessageKeys.NOT_ENOUGH_LEVEL.getMessage());
    }
}
