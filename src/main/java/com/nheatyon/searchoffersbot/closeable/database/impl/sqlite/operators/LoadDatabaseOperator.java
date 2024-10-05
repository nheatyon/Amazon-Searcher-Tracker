package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators;

import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseConcreteExecutor;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;

public final class LoadDatabaseOperator extends DatabaseConcreteExecutor implements DatabaseOperator {

    public LoadDatabaseOperator(ConfigurationManager<?, String> config, OperatorFactory operators) {
        super(config, operators);
    }

    @Override
    public void set(String userId, Object... values) {
        executeUpdate("CREATE TABLE IF NOT EXISTS " + getTableName()
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id BIGINT, "
                + "full_name TEXT, username TEXT, edit_id BIGINT,"
                + "is_admin TINYINT(1), is_searching TINYINT(1), "
                + "is_tracking TINYINT(1));", false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String... data) {
        return (T) new Object();
    }
}
