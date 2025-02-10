package net.solostudio.skillgrind.utils;

import lombok.experimental.UtilityClass;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.commands.CommandGrindstone;
import net.solostudio.skillgrind.enums.keys.ConfigKeys;
import net.solostudio.skillgrind.exception.CommandExceptionHandler;
import net.solostudio.skillgrind.listeners.GrindstoneListener;
import net.solostudio.skillgrind.listeners.MenuListener;
import org.bukkit.Bukkit;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.orphan.Orphans;

@UtilityClass
public class RegisterUtils {
    public void registerListeners() {
        LoggerUtils.info("### Registering listeners... ###");

        Bukkit.getPluginManager().registerEvents(new GrindstoneListener(), SkillGrind.getInstance());
        Bukkit.getPluginManager().registerEvents(new MenuListener(), SkillGrind.getInstance());

        LoggerUtils.info("### Successfully registered 2 listener. ###");
    }

    public static void registerCommands() {
        LoggerUtils.info("### Registering commands... ###");

        var lamp = BukkitLamp.builder(SkillGrind.getInstance())
                .exceptionHandler(new CommandExceptionHandler())
                .build();

        lamp.register(Orphans.path(ConfigKeys.ALIASES.getList().toArray(String[]::new)).handler(new CommandGrindstone()));

        LoggerUtils.info("### Successfully registered exception handlers... ###");
    }
}
