package me.nazarxexe.job.admintool.checking;

import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.listener.PlayerListener;
import org.bukkit.entity.Player;

public class CheckSuspend implements Runnable {
    @Override
    public void run() {
        ReportTool.getCache().asMap().forEach((key, val) -> {

            Player player = ReportTool.getInstance().getServer().getPlayer(key);
            if (player == null) return;
            if (val.getReports() < 3) return;
            PlayerListener.getLock().add(key);

        });
    }
}
