package com.nheatyon.searchoffersbot.config;

import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.UserCounterOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class PlaceholderParser {

    private final ConfigurationManager<?, String> config;
    private final OperatorFactory operators;

    public String getCountableMessage() {
        String countableMessage = config.read("total_users");
        DatabaseOperator counterOperator = operators.get(UserCounterOperator.class);
        return countableMessage.replaceAll("%total_users%", counterOperator.get());
    }

    public String getTrackedProductsMessage() {
        String countableMessage = config.read("total_tracks");
        AtomicInteger productsSize = new AtomicInteger();
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            products.forEach((p) -> productsSize.getAndIncrement());
        });
        return countableMessage.replaceAll("%total_tracks%", String.valueOf(productsSize.get()));
    }
}
