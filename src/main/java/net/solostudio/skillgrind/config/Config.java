package net.solostudio.skillgrind.config;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.handlers.ConfigurationHandler;

public class Config extends ConfigurationHandler {
    public Config() {
        super(SkillGrind.getInstance().getDataFolder().getPath(), "config");
        save();
    }
}
