package me.nazarxexe.job.admintool.impl;

import me.nazarxexe.job.admintool.database.data.ReportData;

public interface ICache {
    void add(ReportData data);
    void remove(ReportData data);

    ReportData get(String name);

    void build();

}
