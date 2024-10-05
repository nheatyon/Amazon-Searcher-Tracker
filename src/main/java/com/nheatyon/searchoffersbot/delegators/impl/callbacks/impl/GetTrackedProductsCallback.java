package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl.VariableProduct;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@SuperBuilder
@BotCallback(value = "get_tracked_products")
public final class GetTrackedProductsCallback extends GenericCallback {

    public GetTrackedProductsCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        long parsedUserId = Long.parseLong(getUserId());
        ConfigurationManager<?, String> config = getBot().getConfig();
        String trackedMessage = config.read("tracked_products");
        InlineKeyboardMarkup backButton = getBot().getKeyboards().get(KeyboardsType.BACK_BUTTON);
        List<VariableProduct> variableProducts = ProductTrackerService.getTrackedProducts().get(parsedUserId);
        if (ProductTrackerService.getTrackedProducts() == null || variableProducts == null || variableProducts.isEmpty()) {
            getBot().editMessage(getCallback(), config.read("no_trackable_products"), backButton);
            return;
        }
        // Extract products
        StringBuilder sb = new StringBuilder();
        String template = "\n\n📦 <i><a href=\"%s\">%s</a></i> - <b>[<code>%s</code>]</b>";
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            if (userId != parsedUserId) {
                return;
            }
            products.forEach(p -> sb.append(String.format(template, p.getUrl(), p.getTitle(), p.getAsin())));
        });
        int trackedSize = ProductTrackerService.getTrackedProducts()
                .get(parsedUserId)
                .size();
        String sizeProducts = config.read("size_tracked_products")
                .toString()
                .replaceAll("%size%", String.valueOf(trackedSize));
        String suggestRemove = config.read("suggest_remove_product");
        sb.append("\n\n").append(sizeProducts).append("\n").append(suggestRemove);
        getBot().editMessage(getCallback(), trackedMessage + sb, backButton);
    }
}
