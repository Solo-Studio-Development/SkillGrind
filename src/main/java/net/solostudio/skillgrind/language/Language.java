package net.solostudio.skillgrind.language;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.handlers.ConfigurationHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Language extends ConfigurationHandler {
    public Language(@NotNull String name) {
        super(SkillGrind.getInstance().getDataFolder().getPath() + File.separator + "locales", name);
        save();
    }
}
