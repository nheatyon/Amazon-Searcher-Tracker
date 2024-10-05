package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@BotCallback(value = "terminate_operations")
public final class TerminateOperationsCallback extends GenericCallback {

    public TerminateOperationsCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        OperatorFactory operators = getBot().getOperators();
        DatabaseOperator searchingOperator = operators.get(SearchingOperator.class);
        DatabaseOperator trackingOperator = operators.get(TrackingOperator.class);
        if (searchingOperator.get(getUserId())) {
            searchingOperator.set(getUserId(), false);
        }
        if (trackingOperator.get(getUserId())) {
            trackingOperator.set(getUserId(), false);
        }
        ConfigurationManager<?, String> config = getBot().getConfig();
        getBot().answerCallback(getCallback(), config.read("operation_terminated"));
        getBot().editWelcome(getCallback());
    }
}
