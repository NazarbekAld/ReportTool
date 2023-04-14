package me.nazarxexe.job.admintool.command;

import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.listener.PlayerListener;
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 0) {

            List<String> players = new ArrayList<>();

            ReportTool.getInstance().getServer().getOnlinePlayers().forEach((player -> {
                players.add(player.getName());
            }));

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
            return ReportTool.getQuick_message();
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            // ADMIN
            if (args[0].equals("admin")) {

                if (args[1].equals("list")) {
                    ReportTool.getInstance().list((Player) sender, Integer.valueOf(args[2]) < 1 ? 1 : Integer.valueOf(args[2]));
                }
                if (args[1].equals("suspend")) {

                    StringBuffer buffer = new StringBuffer();

                    for (int i = 0; i < args.length; i++) {
                        if (i < 3) continue;
                        buffer.append(args[i]).append(" ");
                    }

                    ReportTool.getCache().put(args[2], new ReportData(
                            args[2],
                            buffer.toString(),
                            999,
                            System.currentTimeMillis(),
                            new ArrayList<String>()
                    ));
                }
                if (args[1].equals("free")) {
                    PlayerListener.getLock().remove(args[2]);
                    ReportTool.getInstance().free(args[2]);
                }

                if (args[1].equals("cache")){
                    ReportTool.getCache().asMap().forEach((name, report) -> {
                        sender.sendMessage(report.toString());
                    });
                }

            } else {

                if (args[0].equals(((Player) sender).getName())){
                    sender.sendMessage(ChatColor.RED + "Вы не можете зарепортить самого себя!");
                    return true;
                }

                // PLAYER

                StringBuffer buffer = new StringBuffer();

                for (int i = 0; i < args.length; i++) {
                    if (i < 1) continue;
                    buffer.append(args[i]).append(" ");
                }

                if (!(ReportTool.getCache().asMap().containsKey(args[0]))) {
                    ReportTool.getCache().put(args[0], new ReportData(
                            args[0],
                            buffer.toString(),
                            1,
                            System.currentTimeMillis(),
                            new ArrayList<String>() {{
                                add(((Player) sender).getName());
                            }}
                    ));
                } else {

                    if (ReportTool.getCache().asMap().get(args[0]).getReporters().contains(((Player) sender).getName()))
                    {
                        sender.sendMessage(ChatColor.RED + "Вы уже отпраили репорт к игроку!");
                        return true;
                    }
                    ReportData existData = ReportTool.getCache().asMap().get(args[0]);

                    existData.getReporters().add(((Player) sender).getName());
                    existData.setReports(existData.getReports() + 1);
                    ReportTool.getCache().put(args[0], new ReportData(
                            args[0],
                            buffer.toString(),
                            existData.getReports(),
                            System.currentTimeMillis(),
                            existData.getReporters()
                    ));


                }

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
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', new StringBuffer()
                            .append("&cЧто-то призошло не так!\n")
                            .append("&fПопробуйте написать комманду правилно.\n")
                            .append("&f/report <ИГРОК> <ПРИЧИНА...>")
                    .toString()));

            return true;
        }
    }
}
