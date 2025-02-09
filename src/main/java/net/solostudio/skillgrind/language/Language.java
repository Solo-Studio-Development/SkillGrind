package net.solostudio.skillgrind.language;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.manager.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Language extends ConfigurationManager {
    public Language(@NotNull String name) {
        super(SkillGrind.getInstance().getDataFolder().getPath() + File.separator + "locales", name);
        save();
    }
}
