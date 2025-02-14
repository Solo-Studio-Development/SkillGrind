package net.solostudio.skillgrind.handlers;

import lombok.extern.slf4j.Slf4j;
import net.solostudio.skillgrind.processor.MessageProcessor;
import net.solostudio.skillgrind.utils.LoggerUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public record ConfigurationHandler(@NotNull YamlConfiguration yml, @NotNull String name, @NotNull File config) {

    public ConfigurationHandler {}

    @Contract("_, _ -> new")
    public static @NotNull ConfigurationHandler of(@NotNull String dir, @NotNull String name) {
        Path directory = Path.of(dir);
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            LoggerUtils.error("Failed to create directories: " + exception.getMessage());
            throw new RuntimeException(exception);
        }

        Path configPath = directory.resolve(name + ".yml");
        File configFile = configPath.toFile();

        if (!configFile.exists()) {
            try {
                Files.createFile(configPath);
            } catch (IOException exception) {
                LoggerUtils.error("Failed to create config file: " + exception.getMessage());
                throw new RuntimeException(exception);
            }
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        yml.options().copyDefaults(true);

        return new ConfigurationHandler(yml, name, configFile);
    }

    public void reload() {
        YamlConfiguration.loadConfiguration(config).options().copyDefaults(true);
        save();
    }

    public void set(@NotNull String path, @NotNull Object value) {
        yml.set(path, value);
        save();
    }

    public void save() {
        try {
            yml.save(config);
        } catch (IOException exception) {
            LoggerUtils.error("Failed to save config: " + exception.getMessage());
        }
    }

    public List<String> getList(@NotNull String path) {
        return yml.getStringList(path).stream()
                .map(MessageProcessor::process)
                .toList();
    }

    public boolean getBoolean(@NotNull String path) {
        return yml.getBoolean(path);
    }

    public int getInt(@NotNull String path) {
        return yml.getInt(path);
    }

    public String getString(@NotNull String path) {
        return yml.getString(path);
    }

    public @Nullable ConfigurationSection getSection(@NotNull String path) {
        return yml.getConfigurationSection(path);
    }
}