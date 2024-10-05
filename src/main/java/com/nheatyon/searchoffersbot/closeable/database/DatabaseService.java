package com.nheatyon.searchoffersbot.closeable.database;

import com.nheatyon.searchoffersbot.closeable.CloseableService;
import com.nheatyon.searchoffersbot.config.FileCreator;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;

public final class DatabaseService implements CloseableService {

    @Getter private static HikariDataSource source;
    public static final String FILE_NAME = "tables.db";

    void createConnection(String poolName, String driverName, String jdbcUrl) {
        if (source == null) {
            FileCreator creator = new FileCreator();
            File file = creator.create(FILE_NAME);
            source = new HikariDataSource();
            source.setPoolName(poolName);
            source.setDriverClassName(driverName);
            source.setJdbcUrl(jdbcUrl + file.getPath());
            source.addDataSourceProperty("cachePrepStmts", true);
            source.addDataSourceProperty("prepStmtCacheSize", 250);
            source.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            source.addDataSourceProperty("useServerPrepStmts", true);
            source.addDataSourceProperty("useLocalSessionState", true);
            source.addDataSourceProperty("rewriteBatchedStatements", true);
            source.addDataSourceProperty("cacheResultSetMetadata", true);
            source.addDataSourceProperty("cacheServerConfiguration", true);
            source.addDataSourceProperty("elideSetAutoCommits", true);
            source.addDataSourceProperty("maintainTimeStats", false);
        }
    }

    @Override
    public void close() {
        if (source != null) {
            source.close();
        }
    }
}
