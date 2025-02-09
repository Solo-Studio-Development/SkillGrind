package net.solostudio.skillgrind.utils;

import lombok.experimental.UtilityClass;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.listeners.GrindstoneListener;
import net.solostudio.skillgrind.listeners.MenuListener;
import org.bukkit.Bukkit;

@UtilityClass
public class RegisterUtils {
    public void registerListeners() {
        LoggerUtils.info("### Registering listeners... ###");

        Bukkit.getPluginManager().registerEvents(new GrindstoneListener(), SkillGrind.getInstance());
        Bukkit.getPluginManager().registerEvents(new MenuListener(), SkillGrind.getInstance());

        LoggerUtils.info("### Successfully registered 2 listener. ###");
    }
}
