package net.solostudio.skillgrind.config;

import lombok.Getter;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.handlers.ConfigurationHandler;

@Getter
public class Config {
    private final ConfigurationHandler handler;

    public Config() {
        handler = ConfigurationHandler.of(SkillGrind.getInstance().getDataFolder().getPath(), "config");
        handler.save();
    }
}
