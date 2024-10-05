package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl.tracks;

import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl.VariableProduct;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuperBuilder
public final class AddToTrackCallback extends GenericCallback {

    public AddToTrackCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        ConfigurationManager<?, String> config = getBot().getConfig();
        String asin = getCallback().getData().replace("add_to_track_", "");
        AtomicBoolean isAlreadyTracked = new AtomicBoolean(false);
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            isAlreadyTracked.set(products.stream().anyMatch(p -> p.getAsin().equals(asin)));
        });
        if (isAlreadyTracked.get()) {
            getBot().answerCallback(getCallback(), config.read("already_in_tracks"));
            return;
        }
        long parsedUserId = Long.parseLong(getUserId());
        int maxTrackable = ((Long) config.read("max_trackable_limit")).intValue();
        List<VariableProduct> registeredProducts = ProductTrackerService.getTrackedProducts().get(parsedUserId);
        if (registeredProducts != null && registeredProducts.size() >= maxTrackable) {
            getBot().answerCallback(getCallback(), config.read("max_trackable_reached"));
            return;
        }
        ProductAPI api = new ProductAPI(config);
        api.track(getBot(), parsedUserId, asin);
        getBot().answerCallback(getCallback(), config.read("added_to_tracks"));
    }
}
