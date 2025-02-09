package net.solostudio.skillgrind.config;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.manager.ConfigurationManager;

public class Config extends ConfigurationManager {
    public Config() {
        super(SkillGrind.getInstance().getDataFolder().getPath(), "config");
        save();
    }
}
