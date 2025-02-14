package net.solostudio.skillgrind.utils;

import lombok.experimental.UtilityClass;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.enums.VersionTypes;
import net.solostudio.skillgrind.versions.VersionSupport;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.solostudio.skillgrind.enums.VersionTypes.determineVersion;

@UtilityClass
public class StartingUtils {
    private static final int REQUIRED_VM_VERSION = 17;

    public static void initialize() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        registerListenersAndCommands();
        validateEnvironment();
    }

    private static void validateEnvironment() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (!checkVMVersion()) {
            disablePlugin("### Wrong VM version! Required version: " + REQUIRED_VM_VERSION + " or higher. ###");
            return;
        }

        if (!checkVersion()) disablePlugin("### Unsupported server version. Please update your server to a supported version. ###");
    }

    private static boolean checkVMVersion() {
        int vmVersion = getVMVersion();
        if (vmVersion < REQUIRED_VM_VERSION) {
            LoggerUtils.error("### Detected Java version: {}. Required: {} or higher. ###", vmVersion, REQUIRED_VM_VERSION);
            return false;
        }
        return true;
    }

    private static boolean checkVersion() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (!isSpigotConfigPresent()) {
            LoggerUtils.error("### SpigotConfig class not found. This might indicate an unsupported server. ###");
            return false;
        }

        String bukkitVersion = Bukkit.getVersion();
        VersionTypes version = extractVersionFromBukkitString(bukkitVersion);

        LoggerUtils.info("### Detected Bukkit version string: {} ###", bukkitVersion);

        if (version == VersionTypes.UNKNOWN) {
            LoggerUtils.error("### Unknown Minecraft version detected. ###");
            return false;
        }

        return new VersionSupport(version).getVersionSupport() != null;
    }

    private static boolean isSpigotConfigPresent() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static VersionTypes extractVersionFromBukkitString(@NotNull String bukkitVersion) {
        Pattern versionPattern = Pattern.compile("\\(MC: (\\d{1,2})\\.(\\d{1,2})(?:\\.(\\d{1,2}))?\\)");
        Matcher matcher = versionPattern.matcher(bukkitVersion);

        if (matcher.find()) {
            int majorVersion = Integer.parseInt(matcher.group(1));
            int minorVersion = Integer.parseInt(matcher.group(2));
            int patchVersion = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

            return determineVersion(majorVersion, minorVersion, patchVersion);
        }

        LoggerUtils.error("### Could not parse Minecraft version from Bukkit string: {} ###", bukkitVersion);
        return VersionTypes.UNKNOWN;
    }

    private static int getVMVersion() {
        String javaVersion = System.getProperty("java.version");
        Matcher matcher = Pattern.compile("(?:1\\.)?(\\d+)").matcher(javaVersion);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        LoggerUtils.error("### Unable to detect Java version from string: {} ###", javaVersion);
        return -1;
    }

    private static void disablePlugin(@NotNull String errorMessage) {
        LoggerUtils.error(errorMessage);
        Bukkit.getPluginManager().disablePlugin(SkillGrind.getInstance());
    }

    public void saveResourceIfNotExists(@NotNull String resourcePath) {
        File targetFile = new File(SkillGrind.getInstance().getDataFolder(), resourcePath);

        if (!targetFile.exists()) SkillGrind.getInstance().saveResource(resourcePath, false);
    }

    public void registerListenersAndCommands() {
        RegisterUtils.registerListeners();
        RegisterUtils.registerCommands();
    }
}
