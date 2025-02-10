package net.solostudio.skillgrind.commands;

import net.solostudio.skillgrind.SkillGrind;
import net.solostudio.skillgrind.enums.keys.MessageKeys;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandGrindstone implements OrphanCommand {
    @Subcommand("reload")
    @CommandPermission("grindstone.reload")
    public void reload(@NotNull CommandSender sender) {
        SkillGrind.getInstance().getLanguage().reload();
        SkillGrind.getInstance().getConfiguration().reload();
        sender.sendMessage(MessageKeys.RELOAD.getMessage());
    }
}
