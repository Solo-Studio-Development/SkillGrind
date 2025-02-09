package net.solostudio.skillgrind.gui;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import net.solostudio.skillgrind.SkillGrind;
import org.jetbrains.annotations.NotNull;

public class MenuUpdater {
    private final Menu menu;
    private MyScheduledTask task;

    public MenuUpdater(@NotNull Menu menu) {
        this.menu = menu;
    }

    public void start(int intervalTicks) {
        if (isRunning()) return;

        task = SkillGrind.getInstance().getScheduler().runTaskTimer(this::updateMenu, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void updateMenu() {
        if (menu.getInventory().getViewers().isEmpty()) {
            stop();
            return;
        }

        if (menu.getInventory().getViewers().contains(menu.menuData.owner())) menu.updateMenuItems();
        else stop();
    }

    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }
}
