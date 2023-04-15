package me.nazarxexe.job.admintool.database.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import me.nazarxexe.job.admintool.ReportTool;
import me.nazarxexe.job.admintool.database.data.ReportData;
import me.nazarxexe.job.admintool.impl.ICache;
import me.nazarxexe.job.admintool.impl.TableManageable;
import org.checkerframework.checker.index.qual.NonNegative;

public class Cache implements ICache {

    private com.github.benmanes.caffeine.cache.Cache<String, ReportData> cache;
    private final TableManageable table;
    public Cache(TableManageable table) {
        this.table = table;
    }


    @Override
    public void add(ReportData data) {
        cache.put(data.getSuspender(), data);
    }

    @Override
    public void remove(ReportData data) {
        cache.invalidate(data.getSuspender());
    }

    @Override
    public ReportData get(String name) {
        return cache.asMap().get(name);
    }

    @Override
    public void build() {

        cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, ReportData>() {
                    @Override
                    public long expireAfterCreate(String key, ReportData value, long currentTime) {
                        table.insertReportData(value);
                        return currentTime;
                    }

                    @Override
                    public long expireAfterUpdate(String key, ReportData value, long currentTime, @NonNegative long currentDuration) {
                        table.insertReportData(value);
                        return currentTime;
                    }

                    @Override
                    public long expireAfterRead(String key, ReportData value, long currentTime, @NonNegative long currentDuration) {
                        return currentTime;
                    }
                })
                .build();

    }
}
