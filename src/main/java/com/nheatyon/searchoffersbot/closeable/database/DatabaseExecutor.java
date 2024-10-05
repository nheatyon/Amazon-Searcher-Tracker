package com.nheatyon.searchoffersbot.closeable.database;

import java.util.List;

public abstract class DatabaseExecutor {

    public DatabaseExecutor(String poolName, String driverName, String jdbcUrl) {
        DatabaseService database = new DatabaseService();
        database.createConnection(poolName, driverName, jdbcUrl);
    }

    public abstract List<Object> executeQuery(String query, Object... params);
    public abstract void executeUpdate(String query, boolean async, Object... params);
}
