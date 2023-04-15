package me.nazarxexe.job.admintool.impl;

import me.nazarxexe.job.admintool.database.data.ReportData;
import org.bukkit.entity.Player;
import org.jooq.Condition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TableManageable {

    void free(Player player);
    void showList(Player player, int limit);

    CompletableFuture<Boolean> exist(Condition condition);
    CompletableFuture<Boolean> exist(String name);

    CompletableFuture<List<ReportData>> getReportDataByName(String name);

    CompletableFuture<List<ReportData>> getReportDataByCondition(Condition condition);

    void insertReportData(ReportData data);

    void removeReportDataByCondition(Condition condition);
    void removeReportDataByName(String name);

}
