package me.nazarxexe.job.admintool;

import lombok.Getter;
import me.nazarxexe.job.admintool.checking.CheckSuspend;
import me.nazarxexe.job.admintool.command.ReportExecutor;
import me.nazarxexe.job.admintool.command.ReportTabCompleter;
import me.nazarxexe.job.admintool.database.IDatabase;
import me.nazarxexe.job.admintool.database.MySQL;
import me.nazarxexe.job.admintool.database.ReportsTable;
import me.nazarxexe.job.admintool.database.cache.Cache;
import me.nazarxexe.job.admintool.impl.ICache;
import me.nazarxexe.job.admintool.impl.PlayerLockable;
import me.nazarxexe.job.admintool.impl.TableManageable;
import me.nazarxexe.job.admintool.listener.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.SQLException;
import java.util.List;

import static org.jooq.impl.SQLDataType.INTEGER;

@SuppressWarnings({"all"}) //
public final class ReportTool extends JavaPlugin {

    private IDatabase database;

    private TableManageable reportsManager;

    private PlayerLockable playerLocker;

    private ICache cache;

    private static @Getter String SUSPEND_TITLE;
    private static @Getter String SUSPEND_SUBTITLE;
    private static @Getter String SUSPEND_ACTIONBAR;
    private static @Getter String SUSPEND_MESSAGE;
    private static @Getter List<String> QUICK_MESSAGE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        database();
        messages();

        reportsManager = new ReportsTable(this, database);
        cache = new Cache(reportsManager);
        cache.build();

        PlayerListener listener = new PlayerListener(reportsManager);
        playerLocker = (PlayerLockable) listener;

        command();

        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, new CheckSuspend(cache,
                this, playerLocker) ,0L, 20L);
    }

    private void initTable() {
        try {
            DSL.using(database.getConnection(), SQLDialect.MYSQL)
                    .createTableIfNotExists("reports")
                    .column("id", INTEGER.identity(true))
                    .column("player", SQLDataType.VARCHAR(36))
                    .column("message", SQLDataType.VARCHAR(200))
                    .column("reports", INTEGER)
                    .column("date", SQLDataType.TIMESTAMP)
                    .primaryKey(DSL.field("id"))
                    .execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void database() {

        ConfigurationSection section = getConfig().getConfigurationSection("database");

        database = new MySQL(
                section.getString("ip"),
                section.getString("port"),
                section.getString("user"),
                section.getString("password"),
                section.getString("db")
        );
        initTable();
    }

    private void messages() {
        ConfigurationSection section = getConfig().getConfigurationSection("suspend");
        SUSPEND_TITLE = color(section.getString("title"));
        SUSPEND_MESSAGE = color(section.getString("message"));
        SUSPEND_ACTIONBAR = color(section.getString("actionbar"));
        SUSPEND_SUBTITLE = color(section.getString("subtitle"));
        QUICK_MESSAGE = getConfig().getStringList("quick_message");
    }

    private void command() {
        getCommand("report").setExecutor(new ReportExecutor(this, cache, reportsManager, playerLocker));
        getCommand("report").setTabCompleter(new ReportTabCompleter(this));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
