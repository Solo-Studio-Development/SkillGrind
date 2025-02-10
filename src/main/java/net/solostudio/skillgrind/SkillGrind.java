package net.solostudio.skillgrind;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import lombok.Getter;
import net.solostudio.skillgrind.config.Config;
import net.solostudio.skillgrind.enums.LanguageTypes;
import net.solostudio.skillgrind.enums.keys.ConfigKeys;
import net.solostudio.skillgrind.language.Language;
import revxrsal.zapper.ZapperJavaPlugin;

import java.util.Arrays;

import static net.solostudio.skillgrind.utils.StartingUtils.*;

public final class SkillGrind extends ZapperJavaPlugin {
    @Getter private static SkillGrind instance;
    @Getter private TaskScheduler scheduler;
    @Getter private Language language;
    private Config config;

    @Override
    public void onLoad() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initializeComponents();
        registerListenersAndCommands();
    }

    public Config getConfiguration() {
        return config;
    }

    private void initializeComponents() {
        config = new Config();

        Arrays.stream(LanguageTypes.values())
                .toList()
                .stream()
                .filter(type -> type.name().matches(".*[a-z].*"))
                .forEach(type -> {
                    saveResourceIfNotExists("locales/messages_" + type.name() + ".yml");
                });

        saveResourceIfNotExists("config.yml");

        language = new Language("messages_" + LanguageTypes.valueOf(ConfigKeys.LANGUAGE.getString()));
    }
}
