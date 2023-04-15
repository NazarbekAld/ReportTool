package me.nazarxexe.job.admintool.command;

import me.nazarxexe.job.admintool.ReportTool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReportTabCompleter implements TabCompleter {

    private final ReportTool tool;
    public ReportTabCompleter(ReportTool tool) {
        this.tool = tool;
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
}
