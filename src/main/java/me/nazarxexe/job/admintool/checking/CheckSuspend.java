package me.nazarxexe.job.admintool.checking;

import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.impl.ICache;
import me.nazarxexe.job.admintool.impl.PlayerLockable;

public class CheckSuspend implements Runnable {

    private final ICache cache;
    private final ReportTool tool;

    private final PlayerLockable locker;

    public CheckSuspend(ICache cache, ReportTool tool, PlayerLockable locker) {
        this.cache = cache;
        this.tool = tool;
        this.locker = locker;
    }

    @Override
    public void run() {

        tool.getServer().getOnlinePlayers().forEach((player -> {

            ReportData data = cache.get(player.getName());
            if (data == null) return;
            if (data.getReports() < 3) return;
            locker.lock(player.getName());

        }));

    }
}
