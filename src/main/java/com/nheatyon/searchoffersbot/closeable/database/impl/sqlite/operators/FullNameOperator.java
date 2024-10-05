package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators;

import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseConcreteExecutor;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;

public final class FullNameOperator extends DatabaseConcreteExecutor implements DatabaseOperator {

    public FullNameOperator(ConfigurationManager<?, String> config, OperatorFactory operators) {
        super(config, operators);
    }

    @Override
    public void set(String userId, Object... values) {
        executeUpdate("UPDATE " + getTableName() + " SET full_name=? WHERE user_id=?;", true, values[0], userId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String... data) {
        return (T) executeQuery("SELECT full_name FROM " + getTableName() + " WHERE user_id=?;", data[0]).get(0);
    }
}
