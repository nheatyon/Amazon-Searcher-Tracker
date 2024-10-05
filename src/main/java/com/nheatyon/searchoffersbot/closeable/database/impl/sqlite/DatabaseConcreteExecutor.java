package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite;

import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.closeable.database.DatabaseExecutor;
import com.nheatyon.searchoffersbot.closeable.database.DatabaseService;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class DatabaseConcreteExecutor extends DatabaseExecutor {

    private final OperatorFactory operators;
    private final String tableName;

    public DatabaseConcreteExecutor(ConfigurationManager<?, String> config, OperatorFactory operators) {
        super("SQLiteConnectionPool", "org.sqlite.JDBC", "jdbc:sqlite:");
        this.operators = operators;
        this.tableName = config.read("table_name");
    }

    @Override
    @SneakyThrows
    public final List<Object> executeQuery(String query, Object... params) {
        List<Object> result = new ArrayList<>();
        AtomicInteger column = new AtomicInteger(1);
        return AsyncWrapper.supplyAsync(() -> {
            try (Connection conn = DatabaseService.getSource().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int rows = 1;
                    while (rs.next()) {
                        rows++;
                        int columns = rs.getMetaData().getColumnCount();
                        // Parse multiple column items
                        if (!(columns > 1)) {
                            result.add(rs.getObject(1));
                            column.getAndIncrement();
                        }
                        // Parse multiple rows (adding new lists)
                        List<Object> temp = new ArrayList<>();
                        for (int i = 1; i <= columns; i++) {
                            if (rows == 1) {
                                result.add(rs.getObject(i));
                                continue;
                            }
                            temp.add(rs.getObject(i));
                        }
                        if (rows != 1) {
                            result.add(temp);
                        }
                        column.getAndIncrement();
                    }
                }
                return result;
            } catch (SQLException e) {
                Logger.getRootLogger().error(e);
            }
            return Collections.emptyList();
        }).get();
    }

    @Override
    public final void executeUpdate(String query, boolean isAsync, Object... params) {
        Runnable v = () -> {
            try (Connection conn = DatabaseService.getSource().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.getRootLogger().error(e);
            }
        };
        if (!isAsync) {
            v.run();
            return;
        }
        AsyncWrapper.runAsync(v);
    }
}
