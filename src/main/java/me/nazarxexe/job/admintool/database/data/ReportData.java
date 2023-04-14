package me.nazarxexe.job.admintool.database.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@ToString
public class ReportData {

    private String suspender;
    private String message;
    private int reports;

    private long timestamp;

    private List<String> reporters;

    public String getString() {
        return new StringBuffer()
                .append("Ник: ")
                .append(suspender)
                .append("\n    ")
                .append("Причины репорта: ")
                .append(message)
                .append("\n    ")
                .append("Репорты: ")
                .append(reports)
                .append("\n    ")
                .append("Время: ")
                .append(((System.currentTimeMillis() - timestamp) / 1000) / 60)
                .append("минут назад.")
                .toString();
    }



}
