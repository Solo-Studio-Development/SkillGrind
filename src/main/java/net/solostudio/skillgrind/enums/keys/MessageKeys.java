package net.solostudio.skillgrind.enums.keys;

import lombok.Getter;
import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.processor.MessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum MessageKeys {
    RELOAD("messages.reload"),
    NO_PERMISSION("messages.no-permission"),
    NOT_ENOUGH_LEVEL("messages.not-enough-level");

    private final String path;

    MessageKeys(@NotNull final String path) {
        this.path = path;
    }

    public @NotNull String getMessage() {
        return MessageProcessor.process(SkillGrind.getInstance().getLanguage().getString(path))
                .replace("%prefix%", MessageProcessor.process(SkillGrind.getInstance().getLanguage().getString("prefix")));
    }


    public List<String> getMessages() {
        return SkillGrind.getInstance().getLanguage().getList(path)
                .stream()
                .map(MessageProcessor::process)
                .toList();
    }
}
