package com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory;

import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.LoadDatabaseOperator;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperatorFactory {

    private final ConfigurationManager<?, String> config;
    private static OperatorFactory instance;
    private final Map<String, DatabaseOperator> cachedOperators = new HashMap<>();

    public static OperatorFactory getInstance(ConfigurationManager<?, String> config) {
        if (instance == null) {
            instance = new OperatorFactory(config);
            instance.get(LoadDatabaseOperator.class).set(null);
        }
        return instance;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> clazz) {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        return (T) constructor.newInstance(config, instance);
    }

    @SneakyThrows
    public DatabaseOperator get(Class<? extends DatabaseOperator> clazz) {
        String simpleName = clazz.getSimpleName();
        if (cachedOperators.containsKey(simpleName)) {
            return cachedOperators.get(simpleName);
        }
        DatabaseOperator operator = instantiate(clazz);
        cachedOperators.put(simpleName, operator);
        return operator;
    }
}
