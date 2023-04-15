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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Report implements TabCompleter, CommandExecutor {

    private final ReportTool tool;
    private final ICache cache;
    private final TableManageable table;

    private final PlayerLockable locker;

    public Report(ReportTool tool, ICache cache, TableManageable table, PlayerLockable locker) {
        this.tool = tool;
        this.cache = cache;
        this.table = table;
        this.locker = locker;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 0) {

            List<String> players = new ArrayList<>();

            tool.getServer().getOnlinePlayers().forEach((player -> players.add(player.getName())));

            if (sender.hasPermission("nazarxexe.tool.admin")) players.add("admin");

            return players;
        }

        if (args[0].equals("admin") && args.length == 2 && sender.hasPermission("nazarxexe.tool.admin")) {
            return new ArrayList<String>() {{
                add("list");
                add("suspend");
                add("free");
            }};
        }

        if (!(args[0].equals("admin")) && args.length > 1) {
            return ReportTool.getQUICK_MESSAGE();
        }

        return null;
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

                    StringBuffer buffer = new StringBuffer();

                    for (int i = 0; i < args.length; i++) {
                        if (i < 3) continue;
                        buffer.append(args[i]).append(" ");
                    }

                    cache.add(new ReportData(
                            args[2],
                            buffer.toString(),
                            999,
                            System.currentTimeMillis(),
                            new ArrayList<>()
                    ));
                    sender.sendMessage(ChatColor.GREEN + "Вы добавили игрока в спсиок подезриваемых.");
                }
                if (args[1].equals("free")) {
                    locker.unlock(args[2]);
                    table.removeReportDataByName(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Вы удалили игрока из списка подезриваемых!");
                }


            } else {

                // PLAYER
                StringBuffer buffer = new StringBuffer();
                for (int i=0; i < args.length; i++) {
                    if (i < 1) continue;
                    buffer.append(args[i]);
                }

                ReportData data = cache.get(args[0]);

                if (data == null){
                    cache.add(new ReportData(
                            args[0],
                            buffer.toString(),
                            1,
                            System.currentTimeMillis(),
                            new ArrayList<String>() {{ add(sender.getName()); }}
                    ));
                    return true;
                }

                data.setReports(data.getReports() + 1);
                data.setMessage(buffer.toString());
                cache.add(data);

                sender.sendMessage(ChatColor.GREEN + "Вы зарепортили игрока!");

            }

            return true;
        }catch (Exception e) {

            if (sender.hasPermission("nazarxexe.tool.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', new StringBuffer()
                        .append("&cЧто-то призошло не так!\n")
                        .append("&fПопробуйте написать комманду правилно.\n")
                        .append("&f/report <ИГРОК> <ПРИЧИНА...> - Репортить игрока\n")
                        .append("&f/report admin list <лимит> - Получить список репортов\n")
                        .append("&f/report admin suspend/free <ИГРОК> - Снять подозрение(free) или Замарозить (suspend)\n")
                        .toString()));
                e.printStackTrace();
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', new StringBuffer()
                            .append("&cЧто-то призошло не так!\n")
                            .append("&fПопробуйте написать комманду правилно.\n")
                            .append("&f/report <ИГРОК> <ПРИЧИНА...>")
                    .toString()));
            e.printStackTrace();
            return true;
        }
    }
}
