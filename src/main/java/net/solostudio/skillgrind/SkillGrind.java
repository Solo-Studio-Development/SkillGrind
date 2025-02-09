package net.solostudio.skillgrind;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import lombok.Getter;
import net.solostudio.skillgrind.listeners.GeneralEvents;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import revxrsal.zapper.ZapperJavaPlugin;

public final class SkillGrind extends ZapperJavaPlugin {
    @Getter private static SkillGrind instance;
    @Getter private TaskScheduler scheduler;

    @Override
    public void onLoad() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new GeneralEvents(), this);
    }
}
