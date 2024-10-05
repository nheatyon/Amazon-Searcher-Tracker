package com.nheatyon.searchoffersbot.delegators.impl.commands.impl;

import com.nheatyon.searchoffersbot.annotations.BotCommand;
import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl.VariableProduct;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.commands.GenericCommand;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.concurrent.atomic.AtomicReference;

@SuperBuilder
@BotCommand(value = "rimuovi", syntax = "`/rimuovi <asin>`", args = 2)
public final class RemoveCommand extends GenericCommand {

    public RemoveCommand(GenericCommandBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        String asin = getArgs()[1];
        long parsedUserId = Long.parseLong(getUserId());
        ConfigurationManager<?, String> config = getBot().getConfig();
        AtomicReference<VariableProduct> productToRemove = new AtomicReference<>(null);
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            if (userId != parsedUserId) {
                return;
            }
            products.forEach(p -> {
                if (p.getAsin().equals(asin)) {
                    productToRemove.set(p);
                }
            });
        });
        if (productToRemove.get() == null) {
            getBot().sendMessage(getUserId(), config.read("no_product_found"), null);
            return;
        }
        ProductTrackerService.getTrackedProducts()
                .get(parsedUserId)
                .remove(productToRemove.get());
        ProductAPI.getTrackableThreads().get(asin).interrupt();
        InlineKeyboardMarkup homepageButton = getBot().getKeyboards().get(KeyboardsType.HOMEPAGE_BUTTON);
        getBot().sendMessage(getUserId(), config.read("product_removed"), homepageButton);
    }
}
