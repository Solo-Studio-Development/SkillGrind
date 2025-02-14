package net.solostudio.skillgrind.language;

import lombok.Getter;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.handlers.ConfigurationHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Getter
public class Language  {
    private final ConfigurationHandler handler;

    public Language(@NotNull String name) {
        handler = ConfigurationHandler.of(SkillGrind.getInstance().getDataFolder().getPath() + File.separator + "locales", name);
        handler.save();
    }
}
