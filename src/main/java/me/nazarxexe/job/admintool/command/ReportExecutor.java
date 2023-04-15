package me.nazarxexe.job.admintool.command;

import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.impl.ICache;
import me.nazarxexe.job.admintool.impl.PlayerLockable;
import me.nazarxexe.job.admintool.impl.TableManageable;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ReportExecutor implements CommandExecutor {

    private final ReportTool tool;
    private final ICache cache;
    private final TableManageable table;

    private final PlayerLockable locker;

    public ReportExecutor(ReportTool tool, ICache cache, TableManageable table, PlayerLockable locker) {
        this.tool = tool;
        this.cache = cache;
        this.table = table;
        this.locker = locker;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            // ADMIN
            if (args[0].equals("admin")) {

                if (args[1].equals("list")) {
                    table.showList((Player) sender, Integer.parseInt(args[2]));
                }
                if (args[1].equals("suspend")) {
                    suspend(sender, args);
                }
                if (args[1].equals("free")) {
                    free(sender, args);
                }

            } else {

                // PLAYER
                report(sender, args);

            }

            return true;
        }catch (Exception e) {
            if (sender.hasPermission("nazarxexe.tool.admin")) {
                exceptionAdmin(sender);
                e.printStackTrace();
                return true;
            }
            exceptionPlayer(sender);
            e.printStackTrace();
            return true;
        }
    }



    private void suspend(CommandSender sender, String[] args) {
        StringBuffer message = argsToMessage(args, 3);
        cache.add(new ReportData(
                args[2],
                message.toString(),
                999,
                System.currentTimeMillis(),
                new ArrayList<>()
        ));
        sender.sendMessage(ChatColor.GREEN + "Вы добавили игрока в спсиок подезриваемых.");
    }

    private void free(CommandSender sender, String[] args) {
        locker.unlock(args[2]);
        cache.remove(cache.get(args[2]));
        table.removeReportDataByName(args[2]);
        sender.sendMessage(ChatColor.GREEN + "Вы удалили игрока из списка подезриваемых!");
    }

    private void report(CommandSender sender, String[] args) {
        StringBuffer message = argsToMessage(args, 1);
        ReportData data = cache.get(args[0]);
        if (data == null){
            cache.add(new ReportData(
                    args[0],
                    message.toString(),
                    1,
                    System.currentTimeMillis(),
                    new ArrayList<String>() {{ add(sender.getName()); }}
            ));
            return;
        }

        data.setReports(data.getReports() + 1);
        data.setMessage(message.toString());
        cache.add(data);

        sender.sendMessage(ChatColor.GREEN + "Вы зарепортили игрока!");
    }

    private void exceptionAdmin(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', new StringBuffer()
                .append("&cЧто-то призошло не так!\n")
                .append("&fПопробуйте написать комманду правилно.\n")
                .append("&f/report <ИГРОК> <ПРИЧИНА...> - Репортить игрока\n")
                .append("&f/report admin list <лимит> - Получить список репортов\n")
                .append("&f/report admin suspend/free <ИГРОК> - Снять подозрение(free) или Замарозить (suspend)\n")
                .toString()));
    }

    private void exceptionPlayer(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', new StringBuffer()
                .append("&cЧто-то призошло не так!\n")
                .append("&fПопробуйте написать комманду правилно.\n")
                .append("&f/report <ИГРОК> <ПРИЧИНА...>")
                .toString()));
    }





    private StringBuffer argsToMessage(String[] args, int min) {
        StringBuffer buffer = new StringBuffer();
        for (int arg_count=0; arg_count < args.length; arg_count++) {
            if (arg_count < 1) continue;
            buffer.append(args[arg_count]);
        }
        return buffer;
    }
}
