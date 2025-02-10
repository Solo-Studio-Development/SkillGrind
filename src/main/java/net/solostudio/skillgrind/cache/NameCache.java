package net.solostudio.skillgrind.cache;

import org.bukkit.enchantments.Enchantment;

import java.util.WeakHashMap;

public final class NameCache {
    private final WeakHashMap<Enchantment, String> cache = new WeakHashMap<>();

    public String get(Enchantment enchant) {
        return cache.computeIfAbsent(enchant, enchantment ->
                enchantment.getKey().getKey()
                        .replace('_', ' ')
                        .toLowerCase()
        );
    }
}
