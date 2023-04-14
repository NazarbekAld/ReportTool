package me.nazarxexe.job.admintool;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.Getter;
import me.nazarxexe.job.admintool.checking.CheckSuspend;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.database.IDatabase;
import me.nazarxexe.job.admintool.database.MySQL;
import me.nazarxexe.job.admintool.listener.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.SQLDataType.INTEGER;

@SuppressWarnings({"all"}) //
public final class ReportTool extends JavaPlugin {

    private @Getter IDatabase database;

    private @Getter static Cache<String, ReportData> cache;

    private @Getter static ReportTool instance;

    private @Getter static List<String> quick_message;

    private @Getter String suspend_title;
    private @Getter String suspend_subtitle;

    private @Getter String suspend_message;
    private @Getter String suspend_actionbar;

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        saveDefaultConfig();

        ConfigurationSection dbs = getConfig().getConfigurationSection("database");

        database = new MySQL(
                dbs.getString("ip"),
                dbs.getString("port"),
                dbs.getString("user"),
                dbs.getString("password"),
                dbs.getString("db")
        );

        quick_message = getConfig().getStringList("quick_message");
        ConfigurationSection suspend = getConfig().getConfigurationSection("suspend");


        suspend_title = ChatColor.translateAlternateColorCodes('&', suspend.getString("title"));
        suspend_subtitle = ChatColor.translateAlternateColorCodes('&', suspend.getString("subtitle"));
        suspend_message = ChatColor.translateAlternateColorCodes('&', suspend.getString("message"));
        suspend_actionbar = ChatColor.translateAlternateColorCodes('&', suspend.getString("actionbar"));

        getServer().getScheduler().runTaskTimerAsynchronously(this, new CheckSuspend(), 0L, 20L);

        initTable();
        buildCache();

        getCommand("report").setExecutor(new me.nazarxexe.job.admintool.command.Report());
        getCommand("report").setTabCompleter(new me.nazarxexe.job.admintool.command.Report());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

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

    private void buildCache() {
        cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, ReportData>() {
                    @Override
                    public long expireAfterCreate(String name, ReportData reportData, long l) {

                        if (reportData.getReports() < 1) {
                            return l;
                        }

                        getServer().getScheduler().runTaskAsynchronously(ReportTool.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Проверить если репорт уже сушествует.
                                    if (!( DSL.using(database.getConnection(), SQLDialect.MYSQL)
                                            .select()
                                            .from(DSL.table("reports"))
                                            .where(DSL.field("player").eq(name))
                                            .fetch().isEmpty() ))
                                    {
                                        DSL.using(database.getConnection(), SQLDialect.MYSQL)
                                                .update(DSL.table("reports"))
                                                .set(DSL.field("player"), name)
                                                .set(DSL.field("message"), reportData.getMessage())
                                                .set(DSL.field("reports"), reportData.getReports())
                                                .set(DSL.field("date"), new Timestamp(reportData.getTimestamp()))
                                                .where(DSL.field("player").eq(name))
                                                .execute();
                                        return;
                                    }

                                    DSL.using(database.getConnection(), SQLDialect.MYSQL)
                                            .insertInto(DSL.table("reports"))
                                            .set(DSL.field("player"), name)
                                            .set(DSL.field("message"), reportData.getMessage())
                                            .set(DSL.field("reports"), reportData.getReports())
                                            .set(DSL.field("date"), new Timestamp(reportData.getTimestamp()))
                                            .set(DSL.field("date"), DSL.timestamp(new Date(reportData.getTimestamp())))
                                            .execute();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });


                        return l;
                    }

                    @Override
                    public long expireAfterUpdate(String name, ReportData reportData, long currentTime, @NonNegative long currentDuration) {

                        if (reportData.getReports() < 1) {
                            return currentDuration;
                        }

                        getServer().getScheduler().runTaskAsynchronously(ReportTool.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DSL.using(database.getConnection(), SQLDialect.MYSQL)
                                            .update(DSL.table("reports"))
                                            .set(DSL.field("player"), name)
                                            .set(DSL.field("message"), reportData.getMessage())
                                            .set(DSL.field("reports"), reportData.getReports())
                                            .set(DSL.field("date"), new Timestamp(reportData.getTimestamp()))
                                            .where(DSL.field("player").eq(name))
                                            .execute();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        return currentDuration;
                    }


                    @Override
                    public long expireAfterRead(String uuid, ReportData reportData, long l, @NonNegative long l1) {
                        return l1;
                    }
                })
                .build();
    }

    public void free(String player) {

        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                try {

                    cache.invalidate(
                            player
                    );

                    DSL.using(database.getConnection(), SQLDialect.MYSQL)
                            .deleteFrom(DSL.table("reports"))
                            .where(DSL.field("player").eq(player))
                            .execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public void list(Player sender, int limit) {

        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {

                StringBuffer buffer = new StringBuffer();
                buffer.append("Список из ")
                        .append(limit).append(" репортов.\n");
                buffer.append("--------------------- \n");


                try {
                    DSL.using(ReportTool.getInstance().database.getConnection(), SQLDialect.MYSQL)
                            .select()
                            .from(DSL.table("reports"))
                            .orderBy(DSL.field("id"))
                            .limit((limit))
                            .forEach((record -> {
                                buffer.append(new ReportData(
                                                (String) record.get(DSL.field("player")),
                                                (String) record.get(DSL.field("message")),
                                                (Integer) record.get(DSL.field("reports")),
                                                ((Timestamp) record.get(DSL.field("date"))).getTime(),
                                                new ArrayList<>()
                                        )
                                                .getString()
                                ).append("\n");
                            }));
                    buffer.append("--------------------- \n");
                    sender.sendMessage(
                            buffer.toString()
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }

}
