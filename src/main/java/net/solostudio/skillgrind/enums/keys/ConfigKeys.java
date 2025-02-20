package net.solostudio.skillgrind.enums.keys;

import lombok.Getter;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.processor.MessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum ConfigKeys {
    LANGUAGE("language"),
    ALIASES("aliases"),

    GRINDSTONE_TITLE("grindstone.title"),
    GRINDSTONE_CLICK_SOUND("grindstone.click-sound"),

    GRINDSTONE_BEFORE_SYMBOL("grindstone.before-symbol"),
    GRINDSTONE_AFTER_SYMBOL("grindstone.after-symbol"),
    GRINDSTONE_SELECTED_ENCHANT_COLOR("grindstone.selected-enchantment-color"),
    GRINDSTONE_UNSELECTED_ENCHANT_COLOR("grindstone.unselected-enchantment-color"),
    GRINDSTONE_PER_XP_PER_LEVEL("grindstone.price-xp-per-level");

    private final String path;

    ConfigKeys(@NotNull final String path) {
        this.path = path;
    }

    public @NotNull String getString() {
        return MessageProcessor.process(SkillGrind.getInstance().getConfiguration().getString(path));
    }

    public boolean getBoolean() {
        return SkillGrind.getInstance().getConfiguration().getBoolean(path);
    }

    public int getInt() {
        return SkillGrind.getInstance().getConfiguration().getInt(path);
    }

    public List<String> getList() {
        return SkillGrind.getInstance().getConfiguration().getList(path);
    }
}
