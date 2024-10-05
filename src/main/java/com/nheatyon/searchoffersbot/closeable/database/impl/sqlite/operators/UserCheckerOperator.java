package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators;

import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseConcreteExecutor;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;

public final class UserCheckerOperator extends DatabaseConcreteExecutor implements DatabaseOperator {

    public UserCheckerOperator(ConfigurationManager<?, String> config, OperatorFactory operators) {
        super(config, operators);
    }

    @Override
    public void set(String userId, Object... values) {
        String fullName = values[0].toString();
        String username = values[1].toString();
        DatabaseOperator userNotExistsOperator = getOperators().get(UserNotExistsOperator.class);
        if (userNotExistsOperator.get(userId)) {
            executeUpdate("INSERT INTO " + getTableName()
                    + "(user_id, full_name, username, edit_id, is_admin, is_searching, is_tracking) "
                    + "VALUES(?, ?, ?, 0, 0, 0, 0);", false, userId, fullName, username);
            return;
        }
        // Check for name changes
        DatabaseOperator fullNameOperator = getOperators().get(FullNameOperator.class);
        DatabaseOperator usernameOperator = getOperators().get(UsernameOperator.class);
        if (!fullName.equals(fullNameOperator.get(userId))) {
            executeUpdate("UPDATE " + getTableName() + " SET full_name=? WHERE user_id=?;", false, fullName, userId);
        }
        if (username != null && !username.equals(usernameOperator.get(userId))) {
            executeUpdate("UPDATE " + getTableName() + " SET username=? WHERE user_id=?;", false, username, userId);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String... data) {
        return (T) new Object();
    }
}
