package net.solostudio.skillgrind.utils;

import lombok.experimental.UtilityClass;
import net.solostudio.skillgrind.SkillGrind;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@UtilityClass
public class StartingUtils {
    public void saveResourceIfNotExists(@NotNull String resourcePath) {
        File targetFile = new File(SkillGrind.getInstance().getDataFolder(), resourcePath);

        if (!targetFile.exists()) SkillGrind.getInstance().saveResource(resourcePath, false);
    }

    public void registerListenersAndCommands() {
        RegisterUtils.registerListeners();
        RegisterUtils.registerCommands();
    }
}
