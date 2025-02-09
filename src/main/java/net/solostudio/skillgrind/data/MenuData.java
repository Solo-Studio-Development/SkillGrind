package net.solostudio.skillgrind.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record MenuData(@NotNull Player owner) {
    private static final Map<Player, MenuData> menuMap = new ConcurrentHashMap<>();

    public static MenuData getMenuUtils(@NotNull Player player) {
        return menuMap.computeIfAbsent(player, MenuData::new);
    }
}
