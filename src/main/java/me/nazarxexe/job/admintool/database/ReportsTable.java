package me.nazarxexe.job.admintool.database;

import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.impl.TableManageable;
import org.bukkit.entity.Player;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReportsTable implements TableManageable {

    private final ReportTool plugin;
    private final IDatabase database;

    public final static Table<?> TABLE = DSL.table("reports");
    public final static Field<String> PLAYER = DSL.field("player", String.class);
    public final static Field<String> MESSAGE = DSL.field("message", String.class);
    public final static Field<Integer> REPORTS = DSL.field("reports", Integer.class);
    public final static Field<Timestamp> DATE = DSL.field("date", Timestamp.class);

    public ReportsTable(ReportTool plugin, IDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public void free(Player player) {
        removeReportDataByName(player.getName());
    }

    @Override
    public void showList(Player player, int limit) {
        // Брато из старого кода.
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {

                StringBuffer buffer = new StringBuffer();
                buffer.append("Список из ")
                        .append(limit).append(" репортов.\n");
                buffer.append("--------------------- \n");
                try {
                    DSL.using(database.getConnection(), SQLDialect.MYSQL)
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
                    player.sendMessage(
                            buffer.toString()
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     *
     * Проверить что игрок уже сушествует в базе при условий.
     *
     * @param condition Условия запроса.
     * @return Промис CompletableFuture(Boolean)
     */
    @Override
    public CompletableFuture<Boolean> exist(Condition condition) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return DSL.using(database.getConnection(), SQLDialect.MYSQL)
                        .select(DSL.field("player"))
                        .from(TABLE)
                        .where(condition)
                        .fetchOne() != null;
            } catch (SQLException e) { return false; }
        });
    }

    @Override
    public CompletableFuture<Boolean> exist(String name) {
        return exist(PLAYER.eq(name));
    }


    @Override
    public CompletableFuture<List<ReportData>> getReportDataByName(String name) {
        return getReportDataByCondition(PLAYER.eq(name));
    }

    @Override
    public CompletableFuture<List<ReportData>> getReportDataByCondition(Condition condition) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ReportData> datas = new ArrayList<>();
                DSL.using(database.getConnection(), SQLDialect.MYSQL)
                        .select()
                        .from(TABLE)
                        .where(condition)
                        .fetch()
                        .forEach((record -> {
                            datas.add(
                                    new ReportData(
                                            record.get(PLAYER, String.class),
                                            record.get(MESSAGE, String.class),
                                            record.get(REPORTS, Integer.class),
                                            record.get(DATE, Timestamp.class).getTime(),
                                            new ArrayList<>()
                                    )
                            );
                        }));
                return datas;
            } catch (SQLException e) {
                return new ArrayList<>();
            }

        });
    }

    /**
     *
     * Одновремено добовляет и заменяет репорт из БД.
     *
     * @param data
     */
    @Override
    public void insertReportData(ReportData data) {

        exist(data.getSuspender())
                .thenAcceptAsync((exist) -> {
                   try {
                       if (exist) {
                           data.setReports(data.getReports() + 1);
                           DSL.using(database.getConnection(), SQLDialect.MYSQL)
                                   .update(TABLE)
                                   .set(MESSAGE, data.getMessage())
                                   .set(DATE, new Timestamp(System.currentTimeMillis()))
                                   .set(REPORTS, data.getReports())
                                   .where(data.getSuspender())
                                   .execute();
                           return;
                       }
                       DSL.using(database.getConnection(), SQLDialect.MYSQL)
                               .insertInto(TABLE)
                               .set(PLAYER, data.getSuspender())
                               .set(MESSAGE, data.getMessage())
                               .set(REPORTS, data.getReports())
                               .set(DATE, new Timestamp(data.getTimestamp()))
                               .execute();
                   } catch (SQLException e) { return; }
                }).join();

    }

    @Override
    public void removeReportDataByCondition(Condition condition) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    DSL.using(database.getConnection(), SQLDialect.MYSQL)
                            .deleteFrom(TABLE)
                            .where(condition)
                            .execute();
                } catch (SQLException e) { return; }
            }
        });
    }

    @Override
    public void removeReportDataByName(String name) {
        removeReportDataByCondition(PLAYER.eq(name));
    }
}
