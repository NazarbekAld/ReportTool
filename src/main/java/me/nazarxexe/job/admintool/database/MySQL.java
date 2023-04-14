package me.nazarxexe.job.admintool.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL implements IDatabase {

    private Connection cache;

    private String ip;
    private String port;
    private String user;
    private String password;
    private String database;

    public MySQL(String ip, String port, String user, String password, String database) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    @Override
    public Connection getConnection() throws SQLException {

        if (cache != null)
            if (!(cache.isClosed()))
                return cache;

        cache = DriverManager
                .getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database, user, password);
        return cache;
    }
}
