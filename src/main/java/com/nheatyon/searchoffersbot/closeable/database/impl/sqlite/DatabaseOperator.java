package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite;

public interface DatabaseOperator {

    void set(String userId, Object... values);
    <T> T get(String... data);
}
